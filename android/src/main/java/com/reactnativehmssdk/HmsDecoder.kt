package com.reactnativehmssdk

import com.facebook.react.bridge.*
import live.hms.video.connection.stats.quality.HMSNetworkQuality
import live.hms.video.error.HMSException
import live.hms.video.media.settings.HMSAudioTrackSettings
import live.hms.video.media.settings.HMSVideoResolution
import live.hms.video.media.settings.HMSVideoTrackSettings
import live.hms.video.media.tracks.*
import live.hms.video.sdk.models.*
import live.hms.video.sdk.models.role.*
import live.hms.video.sdk.models.trackchangerequest.HMSChangeTrackStateRequest

object HmsDecoder {

  fun getHmsRoom(hmsRoom: HMSRoom?): WritableMap {
    val room: WritableMap = Arguments.createMap()
    if (hmsRoom != null) {
      room.putString("id", hmsRoom.roomId)
      room.putString("name", hmsRoom.name)
      room.putString("metaData", null)
      room.putString("startedAt", hmsRoom.startedAt.toString())
      room.putMap(
          "browserRecordingState",
          this.getHMSBrowserRecordingState(hmsRoom.browserRecordingState)
      )
      room.putMap(
          "rtmpHMSRtmpStreamingState",
          this.getHMSRtmpStreamingState(hmsRoom.rtmpHMSRtmpStreamingState)
      )
      room.putMap(
          "serverRecordingState",
          this.getHMSServerRecordingState(hmsRoom.serverRecordingState)
      )
      room.putMap("hlsStreamingState", this.getHMSHlsStreamingState(hmsRoom.hlsStreamingState))
      room.putMap("localPeer", this.getHmsLocalPeer(hmsRoom.localPeer))
      room.putArray("peers", this.getAllPeers(hmsRoom.peerList))
      room.putInt("peerCount", hmsRoom.peerCount)
    }
    return room
  }

  fun getHmsPeer(hmsPeer: HMSPeer?): WritableMap {
    val peer: WritableMap = Arguments.createMap()
    if (hmsPeer != null) {
      peer.putString("peerID", hmsPeer.peerID)
      peer.putString("name", hmsPeer.name)
      peer.putBoolean("isLocal", hmsPeer.isLocal)
      peer.putString("customerUserID", hmsPeer.customerUserID)
      peer.putString("metadata", hmsPeer.metadata)
      peer.putMap("networkQuality", this.getHmsNetworkQuality(hmsPeer.networkQuality))
      peer.putMap("audioTrack", this.getHmsAudioTrack(hmsPeer.audioTrack))
      peer.putMap("videoTrack", this.getHmsVideoTrack(hmsPeer.videoTrack))
      peer.putMap("role", this.getHmsRole(hmsPeer.hmsRole))
      peer.putArray("auxiliaryTracks", this.getAllTracks(hmsPeer.auxiliaryTracks))
    }
    return peer
  }

  private fun getHmsAudioTrack(hmsAudioTrack: HMSAudioTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (hmsAudioTrack != null) {
      hmsTrack.putString("type", hmsAudioTrack.type.name)
      hmsTrack.putString("trackId", hmsAudioTrack.trackId)
      hmsTrack.putString("source", hmsAudioTrack.source)
      hmsTrack.putString("trackDescription", hmsAudioTrack.description)
      hmsTrack.putBoolean("isMute", hmsAudioTrack.isMute)
    }
    return hmsTrack
  }

  private fun getHmsVideoTrack(hmsVideoTrack: HMSVideoTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (hmsVideoTrack != null) {
      hmsTrack.putString("type", hmsVideoTrack.type.name)
      hmsTrack.putString("trackId", hmsVideoTrack.trackId)
      hmsTrack.putString("source", hmsVideoTrack.source)
      hmsTrack.putString("trackDescription", hmsVideoTrack.description)
      hmsTrack.putBoolean("isMute", hmsVideoTrack.isMute)
      hmsTrack.putBoolean("isDegraded", hmsVideoTrack.isDegraded)
    }
    return hmsTrack
  }

  fun getHmsTrack(track: HMSTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (track != null) {
      hmsTrack.putString("trackId", track.trackId)
      hmsTrack.putString("source", track.source)
      hmsTrack.putString("trackDescription", track.description)
      hmsTrack.putBoolean("isMute", track.isMute)
      hmsTrack.putString("type", track.type.name)
    }
    return hmsTrack
  }

  fun getAllRoles(roles: List<HMSRole>?): WritableArray {
    val decodedRoles: WritableArray = Arguments.createArray()
    if (roles != null) {
      for (role in roles) {
        val decodedRole = this.getHmsRole(role)
        decodedRoles.pushMap(decodedRole)
      }
    }
    return decodedRoles
  }

  private fun getHmsRole(hmsRole: HMSRole?): WritableMap {
    val role: WritableMap = Arguments.createMap()
    if (hmsRole != null) {
      role.putString("name", hmsRole.name)
      role.putMap("permissions", this.getHmsPermissions(hmsRole.permission))
      role.putMap("publishSettings", this.getHmsPublishSettings(hmsRole.publishParams))
      role.putMap("subscribeSettings", this.getHmsSubscribeSettings(hmsRole.subscribeParams))
      role.putInt("priority", hmsRole.priority)
    }
    return role
  }

  private fun getHmsPermissions(hmsPermissions: PermissionsParams?): WritableMap {
    val permissions: WritableMap = Arguments.createMap()
    if (hmsPermissions != null) {
      permissions.putBoolean("endRoom", hmsPermissions.endRoom)
      permissions.putBoolean("removeOthers", hmsPermissions.removeOthers)
      permissions.putBoolean("mute", hmsPermissions.mute)
      permissions.putBoolean("changeRoleForce", hmsPermissions.changeRoleForce)
      permissions.putBoolean("unmute", hmsPermissions.unmute)
      permissions.putBoolean("recording", hmsPermissions.recording)
      permissions.putBoolean("streaming", hmsPermissions.streaming)
      permissions.putBoolean("changeRole", hmsPermissions.changeRole)
    }
    return permissions
  }

  private fun getHmsPublishSettings(hmsPublishSettings: PublishParams?): WritableMap {
    val publishSettings: WritableMap = Arguments.createMap()
    if (hmsPublishSettings != null) {
      publishSettings.putMap("audio", this.getHmsAudioSettings(hmsPublishSettings.audio))
      publishSettings.putMap("video", this.getHmsVideoSettings(hmsPublishSettings.video))
      publishSettings.putMap("screen", this.getHmsVideoSettings(hmsPublishSettings.screen))
      publishSettings.putMap("videoSimulcastLayers", null)
      publishSettings.putMap("screenSimulcastLayers", null)
      publishSettings.putArray("allowed", this.getWriteableArray(hmsPublishSettings.allowed))
    }
    return publishSettings
  }

  private fun getWriteableArray(array: List<String>?): WritableArray {
    val decodedArray: WritableArray = Arguments.createArray()
    if (array != null) {
      for (value in array) {
        decodedArray.pushString(value)
      }
    }
    return decodedArray
  }

  private fun getHmsAudioSettings(hmsAudioSettings: AudioParams?): WritableMap {
    val audioSettings: WritableMap = Arguments.createMap()
    if (hmsAudioSettings != null && hmsAudioSettings.codec != null) {
      audioSettings.putInt("bitRate", hmsAudioSettings.bitRate)
      audioSettings.putString("codec", hmsAudioSettings.codec.name)
    }
    return audioSettings
  }

  private fun getHmsVideoSettings(hmsVideoSettings: VideoParams?): WritableMap {
    val videoSettings: WritableMap = Arguments.createMap()
    if (hmsVideoSettings != null && hmsVideoSettings.codec != null) {
      videoSettings.putInt("bitRate", hmsVideoSettings.bitRate)
      videoSettings.putInt("frameRate", hmsVideoSettings.frameRate)
      videoSettings.putInt("width", hmsVideoSettings.width)
      videoSettings.putInt("height", hmsVideoSettings.height)
      videoSettings.putString("codec", hmsVideoSettings.codec.name)
    }
    return videoSettings
  }

  fun getHmsLocalPeer(hmsLocalPeer: HMSLocalPeer?): WritableMap {
    val peer: WritableMap = Arguments.createMap()
    if (hmsLocalPeer != null) {
      peer.putString("peerID", hmsLocalPeer.peerID)
      peer.putString("name", hmsLocalPeer.name)
      peer.putBoolean("isLocal", hmsLocalPeer.isLocal)
      peer.putString("customerUserID", hmsLocalPeer.customerUserID)
      peer.putString("metadata", hmsLocalPeer.metadata)
      peer.putMap("networkQuality", this.getHmsNetworkQuality(hmsLocalPeer.networkQuality))
      peer.putMap("audioTrack", this.getHmsAudioTrack(hmsLocalPeer.audioTrack))
      peer.putMap("videoTrack", this.getHmsVideoTrack(hmsLocalPeer.videoTrack))
      peer.putMap("role", this.getHmsRole(hmsLocalPeer.hmsRole))
      peer.putArray("auxiliaryTracks", this.getAllTracks(hmsLocalPeer.auxiliaryTracks))
      peer.putMap("localAudioTrackData", this.getHmsLocalAudioTrack(hmsLocalPeer.audioTrack))
      peer.putMap("localVideoTrackData", this.getHmsLocalVideoTrack(hmsLocalPeer.videoTrack))
    }
    return peer
  }

  private fun getHmsLocalAudioTrack(track: HMSLocalAudioTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (track != null) {
      hmsTrack.putDouble("volume", track.volume)
      hmsTrack.putMap("settings", this.getHmsAudioTrackSettings(track.settings))
      hmsTrack.putString("trackId", track.trackId)
      hmsTrack.putString("source", track.source)
      hmsTrack.putString("trackDescription", track.description)
      hmsTrack.putBoolean("isMute", track.isMute)
      hmsTrack.putString("type", track.type.name)
    }
    return hmsTrack
  }

  private fun getHmsLocalVideoTrack(track: HMSLocalVideoTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (track != null) {
      hmsTrack.putBoolean("isDegraded", track.isDegraded)
      hmsTrack.putMap("settings", this.getHmsVideoTrackSettings(track.settings))
      hmsTrack.putString("trackId", track.trackId)
      hmsTrack.putString("source", track.source)
      hmsTrack.putString("trackDescription", track.description)
      hmsTrack.putBoolean("isMute", track.isMute)
      hmsTrack.putString("type", track.type.name)
    }
    return hmsTrack
  }

  private fun getHmsAudioTrackSettings(hmsAudioTrackSettings: HMSAudioTrackSettings?): WritableMap {
    val settings: WritableMap = Arguments.createMap()
    if (hmsAudioTrackSettings != null) {
      settings.putInt("maxBitrate", hmsAudioTrackSettings.maxBitrate)
      settings.putDouble("volume", hmsAudioTrackSettings.volume)
      settings.putBoolean(
          "useHardwareAcousticEchoCanceler",
          hmsAudioTrackSettings.useHardwareAcousticEchoCanceler
      )
      settings.putString("codec", hmsAudioTrackSettings.codec.name)
      settings.putString("trackDescription", "")
    }
    return settings
  }

  private fun getHmsVideoTrackSettings(hmsVideoTrackSettings: HMSVideoTrackSettings?): WritableMap {
    val settings: WritableMap = Arguments.createMap()
    if (hmsVideoTrackSettings != null) {
      settings.putString("codec", hmsVideoTrackSettings.codec.name)
      settings.putInt("maxBitrate", hmsVideoTrackSettings.maxBitRate)
      settings.putInt("maxFrameRate", hmsVideoTrackSettings.maxFrameRate)
      settings.putString("cameraFacing", hmsVideoTrackSettings.cameraFacing.name)
      settings.putString("trackDescription", hmsVideoTrackSettings.codec.name)
      settings.putMap(
          "resolution",
          this.getHmsVideoTrackResolution(hmsVideoTrackSettings.resolution)
      )
    }
    return settings
  }

  private fun getHmsVideoTrackResolution(hmsResolution: HMSVideoResolution): WritableMap {
    val resolution: WritableMap = Arguments.createMap()
    resolution.putInt("height", hmsResolution.height)
    resolution.putInt("width", hmsResolution.width)
    return resolution
  }

  fun getHmsRemotePeers(remotePeers: Array<HMSRemotePeer>?): WritableArray {
    val peers: WritableArray = Arguments.createArray()
    if (remotePeers != null) {
      for (peer in remotePeers) {
        peers.pushMap(this.getHmsRemotePeer(peer))
      }
    }
    return peers
  }

  fun getHmsRemotePeer(hmsRemotePeer: HMSRemotePeer?): WritableMap {
    val peer: WritableMap = Arguments.createMap()
    if (hmsRemotePeer != null) {
      peer.putString("peerID", hmsRemotePeer.peerID)
      peer.putString("name", hmsRemotePeer.name)
      peer.putBoolean("isLocal", hmsRemotePeer.isLocal)
      peer.putString("customerUserID", hmsRemotePeer.customerUserID)
      peer.putString("metadata", hmsRemotePeer.metadata)
      peer.putMap("networkQuality", this.getHmsNetworkQuality(hmsRemotePeer.networkQuality))
      peer.putMap("audioTrack", this.getHmsAudioTrack(hmsRemotePeer.audioTrack))
      peer.putMap("videoTrack", this.getHmsVideoTrack(hmsRemotePeer.videoTrack))
      peer.putMap("role", this.getHmsRole(hmsRemotePeer.hmsRole))
      peer.putArray("auxiliaryTracks", this.getAllTracks(hmsRemotePeer.auxiliaryTracks))
      peer.putMap("remoteAudioTrackData", this.getHmsRemoteAudioTrack(hmsRemotePeer.audioTrack))
      peer.putMap("remoteVideoTrackData", this.getHmsRemoteVideoTrack(hmsRemotePeer.videoTrack))
    }
    return peer
  }

  private fun getHmsRemoteAudioTrack(track: HMSRemoteAudioTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (track != null) {
      hmsTrack.putBoolean("isPlaybackAllowed", track.isPlaybackAllowed)
      hmsTrack.putString("trackId", track.trackId)
      hmsTrack.putString("source", track.source)
      hmsTrack.putString("trackDescription", track.description)
      hmsTrack.putBoolean("isMute", track.isMute)
      hmsTrack.putString("type", track.type.name)
    }
    return hmsTrack
  }

  private fun getHmsRemoteVideoTrack(track: HMSRemoteVideoTrack?): WritableMap {
    val hmsTrack: WritableMap = Arguments.createMap()
    if (track != null) {
      hmsTrack.putBoolean("isDegraded", track.isDegraded)
      hmsTrack.putBoolean("isPlaybackAllowed", track.isPlaybackAllowed)
      hmsTrack.putString("trackId", track.trackId)
      hmsTrack.putString("source", track.source)
      hmsTrack.putString("trackDescription", track.description)
      hmsTrack.putBoolean("isMute", track.isMute)
      hmsTrack.putString("type", track.type.name)
    }
    return hmsTrack
  }

  fun getPreviewTracks(tracks: Array<HMSTrack>?): WritableMap {
    val hmsTracks: WritableMap = Arguments.createMap()
    if (tracks != null) {
      for (track: HMSTrack in tracks) {
        if (track is HMSLocalVideoTrack) {
          hmsTracks.putMap("videoTrack", this.getHmsLocalVideoTrack(track))
        }
        if (track is HMSLocalAudioTrack) {
          hmsTracks.putMap("audioTrack", this.getHmsLocalAudioTrack(track))
        }
      }
    }
    return hmsTracks
  }

  fun getHmsRoleChangeRequest(request: HMSRoleChangeRequest, id: String?): WritableMap {
    val roleChangeRequest: WritableMap = Arguments.createMap()
    if (id != null) {
      roleChangeRequest.putMap("requestedBy", this.getHmsPeer(request.requestedBy))
      roleChangeRequest.putMap("suggestedRole", this.getHmsRole(request.suggestedRole))
      roleChangeRequest.putString("id", id)
      return roleChangeRequest
    }
    return roleChangeRequest
  }

  fun getHmsChangeTrackStateRequest(request: HMSChangeTrackStateRequest, id: String): WritableMap {
    val changeTrackStateRequest: WritableMap = Arguments.createMap()

    changeTrackStateRequest.putMap("requestedBy", this.getHmsPeer(request.requestedBy))
    changeTrackStateRequest.putString("trackType", request.track.type.name)
    changeTrackStateRequest.putBoolean("mute", request.mute)
    changeTrackStateRequest.putString("id", id)

    return changeTrackStateRequest
  }

  fun getError(error: HMSException): WritableMap {
    val decodedError: WritableMap = Arguments.createMap()

    decodedError.putInt("code", error.code)
    decodedError.putString("localizedDescription", error.localizedMessage)
    decodedError.putString("description", error.description)
    decodedError.putString("message", error.message)
    decodedError.putString("name", error.name)
    decodedError.putString("action", error.action)

    return decodedError
  }

  private fun getCustomError(message: String?, code: Int?): WritableMap {
    val decodedError: WritableMap = Arguments.createMap()
    var customCode = 101
    var customMessage = "SOMETHING WENT WRONG"
    if (code !== null) {
      customCode = code.toInt()
    }
    if (message !== null) {
      customMessage = message
    }
    decodedError.putInt("code", customCode)
    decodedError.putString("localizedDescription", customMessage)
    decodedError.putString("description", customMessage)
    decodedError.putString("message", customMessage)
    decodedError.putInt("name", customCode)
    decodedError.putInt("action", customCode)

    return decodedError
  }

  private fun getHMSBrowserRecordingState(data: HMSBrowserRecordingState?): ReadableMap {
    val input = Arguments.createMap()
    if (data !== null) {
      input.putBoolean("running", data.running)
      input.putString("startedAt", data.startedAt.toString())
      input.putString("stoppedAt", data.stoppedAt.toString())
      input.putBoolean("running", data.running)
      input.putMap("error", this.getCustomError(data.error?.message, data.error?.code))
    }
    return input
  }

  private fun getHMSRtmpStreamingState(data: HMSRtmpStreamingState?): ReadableMap {
    val input = Arguments.createMap()
    if (data !== null) {
      input.putBoolean("running", data.running)
      input.putString("startedAt", data.startedAt.toString())
      input.putString("stoppedAt", data.stoppedAt.toString())
      input.putMap("error", this.getCustomError(data.error?.message, data.error?.code))
    }
    return input
  }

  private fun getHMSServerRecordingState(data: HMSServerRecordingState?): ReadableMap {
    val input = Arguments.createMap()
    if (data !== null) {
      input.putBoolean("running", data.running)
      input.putString("startedAt", data.startedAt.toString())
      input.putMap("error", data.error?.let { this.getError(it) })
    }
    return input
  }

  private fun getHMSHlsStreamingState(data: HMSHLSStreamingState?): ReadableMap {
    val input = Arguments.createMap()
    if (data !== null) {
      input.putBoolean("running", data.running)
      input.putArray("variants", this.getHMSHLSVariant(data.variants))
    }
    return input
  }

  private fun getHMSHLSVariant(data: ArrayList<HMSHLSVariant>?): ReadableArray {
    val variants = Arguments.createArray()
    if (data !== null) {
      for (variant in data) {
        val input = Arguments.createMap()
        input.putString("hlsStreamUrl", variant.hlsStreamUrl)
        input.putString("meetingUrl", variant.meetingUrl)
        input.putString("metadata", variant.metadata)
        input.putString("startedAt", variant.startedAt.toString())
        variants.pushMap(input)
      }
    }
    return variants
  }

  private fun getHmsSubscribeSettings(hmsSubscribeSettings: SubscribeParams?): WritableMap {
    val subscribeSettings: WritableMap = Arguments.createMap()
    if (hmsSubscribeSettings != null) {
      subscribeSettings.putInt("maxSubsBitRate", hmsSubscribeSettings.maxSubsBitRate)
      subscribeSettings.putMap(
          "subscribeDegradationParam",
          this.getHmsSubscribeDegradationSettings(hmsSubscribeSettings.subscribeDegradationParam)
      )
      subscribeSettings.putArray(
          "subscribeTo",
          this.getWriteableArray(hmsSubscribeSettings.subscribeTo)
      )
    }
    return subscribeSettings
  }

  private fun getHmsSubscribeDegradationSettings(
      hmsSubscribeDegradationParams: SubscribeDegradationParams?
  ): WritableMap {
    val subscribeDegradationParams: WritableMap = Arguments.createMap()
    if (hmsSubscribeDegradationParams != null) {
      subscribeDegradationParams.putString(
          "degradeGracePeriodSeconds",
          hmsSubscribeDegradationParams.degradeGracePeriodSeconds.toString()
      )
      subscribeDegradationParams.putString(
          "packetLossThreshold",
          hmsSubscribeDegradationParams.packetLossThreshold.toString()
      )
      subscribeDegradationParams.putString(
          "recoverGracePeriodSeconds",
          hmsSubscribeDegradationParams.recoverGracePeriodSeconds.toString()
      )
    }
    return subscribeDegradationParams
  }

  private fun getAllPeers(peers: Array<HMSPeer>?): WritableArray {
    val decodedPeers: WritableArray = Arguments.createArray()
    if (peers != null) {
      for (peer in peers) {
        val decodedPeer = this.getHmsPeer(peer)
        decodedPeers.pushMap(decodedPeer)
      }
    }
    return decodedPeers
  }

  private fun getAllTracks(tracks: MutableList<HMSTrack>?): WritableArray {
    val decodedTracks: WritableArray = Arguments.createArray()
    if (tracks != null) {
      for (track in tracks) {
        val decodedTrack = this.getHmsTrack(track)
        decodedTracks.pushMap(decodedTrack)
      }
    }
    return decodedTracks
  }

  fun getHmsMessageRecipient(recipient: HMSMessageRecipient?): WritableMap {
    val hmsRecipient: WritableMap = Arguments.createMap()
    if (recipient != null) {
      hmsRecipient.putMap("recipientPeer", this.getHmsPeer(recipient.recipientPeer))
      hmsRecipient.putArray("recipientRoles", this.getAllRoles(recipient.recipientRoles))
      hmsRecipient.putString("recipientType", recipient.recipientType.name)
    }
    return hmsRecipient
  }

  private fun getHmsNetworkQuality(networkQuality: HMSNetworkQuality?): WritableMap {
    val hmsNetworkQuality: WritableMap = Arguments.createMap()
    if (networkQuality != null) {
      hmsNetworkQuality.putInt("downlinkQuality", networkQuality.downlinkQuality)
    }
    return hmsNetworkQuality
  }
}
