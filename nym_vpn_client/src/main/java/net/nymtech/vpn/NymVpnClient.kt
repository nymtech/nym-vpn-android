package net.nymtech.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import net.nymtech.logcathelper.LogcatHelper
import net.nymtech.logcathelper.model.LogLevel
import net.nymtech.vpn.model.ClientState
import net.nymtech.vpn.model.Environment
import net.nymtech.vpn.model.ErrorState
import net.nymtech.vpn.model.VpnMode
import net.nymtech.vpn.model.VpnState
import net.nymtech.vpn.util.Constants
import net.nymtech.vpn.util.ServiceManager
import net.nymtech.vpn.util.safeCollect
import nym_vpn_lib.EntryPoint
import nym_vpn_lib.ExitPoint
import nym_vpn_lib.FfiException
import nym_vpn_lib.VpnConfig
import nym_vpn_lib.checkCredential
import nym_vpn_lib.runVpn
import timber.log.Timber

object NymVpnClient {
	private object NymVpnClientInit {
		lateinit var entryPoint: EntryPoint
		lateinit var exitPoint: ExitPoint
		lateinit var mode: VpnMode
		lateinit var environment: Environment
	}

	fun init(
		entryPoint: EntryPoint = EntryPoint.Location(
			Constants.DEFAULT_COUNTRY_ISO,
		),
		exitPoint: ExitPoint = ExitPoint.Location(
			Constants.DEFAULT_COUNTRY_ISO,
		),
		mode: VpnMode = VpnMode.TWO_HOP_MIXNET,
		environment: Environment = Environment.MAINNET,
	): VpnClient {
		synchronized(NymVpnClientInit) {
			NymVpnClientInit.entryPoint = entryPoint
			NymVpnClientInit.exitPoint = exitPoint
			NymVpnClientInit.mode = mode
			NymVpnClientInit.environment = environment
			when (NymVpnClientInit.environment) {
				Environment.MAINNET -> Constants.setupEnvironmentMainnet()
				Environment.SANDBOX -> Constants.setupEnvironmentSandbox()
			}

			return NymVpn
		}
	}
	internal object NymVpn : VpnClient {

		override var entryPoint: EntryPoint = NymVpnClientInit.entryPoint
		override var exitPoint: ExitPoint = NymVpnClientInit.exitPoint
		override var mode: VpnMode = NymVpnClientInit.mode
		private val environment: Environment = NymVpnClientInit.environment

		private val scope = CoroutineScope(Dispatchers.IO)

		private var job: Job? = null

		private val _state = MutableStateFlow(ClientState())
		override val stateFlow: Flow<ClientState> = _state.asStateFlow()

		override fun start(context: Context, credential: String, foreground: Boolean) {
			try {
				checkCredential(credential)
				Timber.i("Credential is valid")
			} catch (e: FfiException.InvalidCredential) {
				Timber.e(e)
				return
			}
			clearErrorStatus()
			job = collectLogStatus(context)
			if (foreground) ServiceManager.startVpnServiceForeground(context) else ServiceManager.startVpnService(context)
		}

		override fun stop(context: Context, foreground: Boolean) {
			ServiceManager.stopVpnService(context)
			job?.cancel()
			_state.value =
				_state.value.copy(
					statistics =
					_state.value.statistics.copy(
						connectionSeconds = null,
					),
				)
		}

		override fun prepare(context: Context): Intent? {
			return VpnService.prepare(context)
		}
		override fun getState(): ClientState {
			return _state.value
		}

		@Synchronized
		private fun clearErrorStatus() {
			_state.value =
				_state.value.copy(
					errorState = ErrorState.None,
				)
		}

		@Synchronized
		private fun setErrorState(message: String) {
			_state.value =
				_state.value.copy(
					errorState = ErrorState.LibraryError(message),
				)
		}

		@Synchronized
		internal fun setVpnState(state: VpnState) {
			_state.value =
				_state.value.copy(
					vpnState = state,
				)
		}

		private fun isTwoHop(mode: VpnMode): Boolean = when (mode) {
			VpnMode.TWO_HOP_MIXNET -> true
			else -> false
		}

		internal fun connect() {
			try {
				runVpn(
					VpnConfig(
						environment.apiUrl,
						environment.explorerUrl,
						entryPoint,
						exitPoint,
						isTwoHop(mode),
					),
				)
			} catch (e: FfiException) {
				Timber.e(e)
			}
		}

		private fun collectLogStatus(context: Context) = scope.launch {
			launch {
				callbackFlow {
					LogcatHelper.logs {
						if (it.level != LogLevel.DEBUG) {
							trySend(it)
						}
					}
					awaitClose { cancel() }
				}.buffer(capacity = 100).safeCollect {
					if (it.tag.contains(Constants.NYM_VPN_LIB_TAG)) {
						when (it.level) {
							LogLevel.ERROR -> {
								if (it.message.contains("Stopped Nym VPN")) {
									setErrorState(it.message)
									stop(context, true)
								}
							}
							LogLevel.INFO -> {
								parseLibInfo(it.message)
							}
							else -> Unit
						}
					}
				}
			}
			launch {
				var seconds = 0L
				do {
					if (_state.value.vpnState == VpnState.Up) {
						_state.value =
							_state.value.copy(
								statistics =
								_state.value.statistics.copy(
									connectionSeconds = seconds,
								),
							)
						seconds++
					}
					delay(1000)
				} while (true)
			}
		}

		private fun parseLibInfo(message: String) {
			// TODO make this more robust in the future
			with(message) {
				when {
					contains("Mixnet processor is running") -> setVpnState(VpnState.Up)
					contains("Setting up connection monitor") -> setVpnState(VpnState.Up)
					contains(
						"Obtaining initial network topology",
					) -> setVpnState(VpnState.Connecting.EstablishingConnection)
				}
			}
		}
	}
}
