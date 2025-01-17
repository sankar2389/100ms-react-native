import React, {useEffect, useRef, useState} from 'react';
import {View, TouchableOpacity, Text, Image, Platform} from 'react-native';
import {
  HMSRemotePeer,
  HMSVideoViewMode,
  HMSPermissions,
  HMSTrack,
  HMSSDK,
  HMSLocalAudioStats,
  HMSLocalVideoStats,
  HMSRTCStatsReport,
} from '@100mslive/react-native-hms';
import Feather from 'react-native-vector-icons/Feather';
import Entypo from 'react-native-vector-icons/Entypo';
import Ionicons from 'react-native-vector-icons/Ionicons';
import {Slider} from '@miblanchard/react-native-slider';
import {useSelector} from 'react-redux';
import RNFS from 'react-native-fs';
import CameraRoll from '@react-native-community/cameraroll';
import Toast from 'react-native-simple-toast';

import {AlertModal, CustomModal, RolePicker} from '../../components';
import dimension from '../../utils/dimension';
import {
  getInitials,
  requestExternalStoragePermission,
} from '../../utils/functions';
import {styles} from './styles';
import type {Peer, LayoutParams} from '../../utils/types';
import type {RootState} from '../../redux';

type DisplayTrackProps = {
  peer: Peer;
  videoStyles: Function;
  instance: HMSSDK | undefined;
  permissions: HMSPermissions | undefined;
  speakerIds?: Array<string>;
  type?: 'local' | 'remote' | 'screen';
  layout?: LayoutParams;
  setChangeNameModal?: Function;
  statsForNerds?: boolean;
  rtcStats?: HMSRTCStatsReport;
  remoteAudioStats?: any;
  remoteVideoStats?: any;
  localAudioStats?: HMSLocalAudioStats;
  localVideoStats?: HMSLocalVideoStats;
  miniView?: boolean;
};

const DisplayTrack = ({
  peer,
  videoStyles,
  speakerIds,
  type,
  instance,
  permissions,
  layout,
  setChangeNameModal,
  statsForNerds,
  remoteAudioStats,
  remoteVideoStats,
  localAudioStats,
  localVideoStats,
  miniView,
}: DisplayTrackProps) => {
  const {
    name,
    trackId,
    colour,
    id,
    peerReference,
    isAudioMute,
    isVideoMute,
    metadata,
  } = peer!;
  const {mirrorLocalVideo} = useSelector((state: RootState) => state.user);
  const [alertModalVisible, setAlertModalVisible] = useState(false);
  const [roleModalVisible, setRoleModalVisible] = useState(false);
  const [newRole, setNewRole] = useState(peerReference?.role);
  const [force, setForce] = useState(false);
  const [volumeModal, setVolumeModal] = useState(false);
  const [volume, setVolume] = useState(1);
  const [isDegraded, setIsDegraded] = useState(false);
  const modalTitle = 'Set Volume';

  const modalButtons: [
    {text: string; onPress?: Function},
    {text: string; onPress?: Function},
  ] = [
    {text: 'Cancel'},
    {
      text: 'Set',
      onPress: () => {
        if (type === 'remote' || type === 'local') {
          instance?.setVolume(peerReference?.audioTrack as HMSTrack, volume);
        } else if (peer?.track) {
          instance?.setVolume(peer?.track, volume);
        }
      },
    },
  ];

  useEffect(() => {
    knownRoles?.map(role => {
      if (role?.name === peerReference?.role?.name) {
        setNewRole(role);
        return;
      }
    });
    const getVolume = async () => {
      if (type === 'local' && !isAudioMute) {
        try {
          setVolume(await instance?.localPeer?.localAudioTrack()?.getVolume());
        } catch (e) {
          console.log('error in get volume', e);
        }
      }
    };
    getVolume();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    setIsDegraded(peerReference?.videoTrack?.isDegraded || false);
  }, [peerReference?.videoTrack?.isDegraded]);

  const HmsView = instance?.HmsView;
  const hmsViewRef: any = useRef();
  const knownRoles = instance?.knownRoles || [];
  const speaking = speakerIds !== undefined ? speakerIds.includes(id!) : false;
  const roleRequestTitle = 'Select action';
  const roleRequestButtons: [
    {text: string; onPress?: Function},
    {text: string; onPress?: Function}?,
  ] = [
    {text: 'Cancel'},
    {
      text: force ? 'Set' : 'Send',
      onPress: async () => {
        await instance?.changeRole(peerReference!, newRole!, force);
      },
    },
  ];

  const selectAuxActionButtons: Array<{
    text: string;
    type?: string;
    onPress?: Function;
  }> = [
    {text: 'Cancel', type: 'cancel'},
    {
      text: 'Set Volume',
      onPress: () => {
        setVolumeModal(true);
      },
    },
  ];

  const selectLocalActionButtons: Array<{
    text: string;
    type?: string;
    onPress?: Function;
  }> = [
    {text: 'Cancel', type: 'cancel'},
    {
      text: 'Change Name',
      onPress: () => {
        setChangeNameModal && setChangeNameModal(true);
      },
    },
  ];

  const selectActionTitle = 'Select action';
  const selectActionMessage = '';
  const selectRemoteActionButtons: Array<{
    text: string;
    type?: string;
    onPress?: Function;
  }> = [
    {text: 'Cancel', type: 'cancel'},
    {
      text: 'Set Volume',
      onPress: () => {
        setVolumeModal(true);
      },
    },
    {
      text: 'Mute/Unmute audio locally',
      onPress: async () => {
        const remotePeer = peerReference as HMSRemotePeer;
        const playbackAllowed = await remotePeer
          ?.remoteAudioTrack()
          ?.isPlaybackAllowed();
        remotePeer?.remoteAudioTrack()?.setPlaybackAllowed(!playbackAllowed);
      },
    },
    {
      text: 'Mute/Unmute video locally',
      onPress: async () => {
        const remotePeer = peerReference as HMSRemotePeer;
        const playbackAllowed = await remotePeer
          ?.remoteVideoTrack()
          ?.isPlaybackAllowed();
        remotePeer?.remoteVideoTrack()?.setPlaybackAllowed(!playbackAllowed);
      },
    },
  ];
  if (permissions?.changeRole) {
    selectLocalActionButtons.push({
      text: 'Change Role',
      onPress: () => {
        setForce(true);
        setRoleModalVisible(true);
      },
    });
    selectRemoteActionButtons.push(
      ...[
        {
          text: 'Prompt to change role',
          onPress: () => {
            setForce(false);
            setRoleModalVisible(true);
          },
        },
        {
          text: 'Force change role',
          onPress: () => {
            setForce(true);
            setRoleModalVisible(true);
          },
        },
      ],
    );
  }
  if (permissions?.removeOthers) {
    selectRemoteActionButtons.push({
      text: 'Remove Participant',
      onPress: async () => {
        await instance?.removePeer(peerReference!, 'removed from room');
      },
    });
  }
  if (permissions?.unmute) {
    const unmute = false;
    if (isAudioMute) {
      selectRemoteActionButtons.push({
        text: 'Unmute audio',
        onPress: async () => {
          await instance?.changeTrackState(
            peerReference?.audioTrack as HMSTrack,
            unmute,
          );
        },
      });
    }
    if (isVideoMute) {
      selectRemoteActionButtons.push({
        text: 'Unmute video',
        onPress: async () => {
          await instance?.changeTrackState(
            peerReference?.videoTrack as HMSTrack,
            unmute,
          );
        },
      });
    }
  }
  if (permissions?.mute) {
    const mute = true;
    if (!isAudioMute) {
      selectRemoteActionButtons.push({
        text: 'Mute audio',
        onPress: async () => {
          await instance?.changeTrackState(
            peerReference?.audioTrack as HMSTrack,
            mute,
          );
        },
      });
    }
    if (!isVideoMute) {
      selectRemoteActionButtons.push({
        text: 'Mute video',
        onPress: async () => {
          await instance?.changeTrackState(
            peerReference?.videoTrack as HMSTrack,
            mute,
          );
        },
      });
    }
  }
  if (Platform.OS === 'android') {
    const takeScreenshot = {
      text: 'Take Screenshot',
      onPress: async () => {
        const granted = await requestExternalStoragePermission();
        if (granted) {
          hmsViewRef?.current
            ?.capture()
            .then(async (d: string) => {
              const imagePath = `${RNFS.DownloadDirectoryPath}image.jpg`;
              RNFS.writeFile(imagePath, d, 'base64')
                .then(() => {
                  CameraRoll.save(imagePath, {type: 'photo'})
                    .then(() => {
                      Toast.showWithGravity(
                        'Image converted to jpg and saved at ' + imagePath,
                        Toast.LONG,
                        Toast.TOP,
                      );
                      console.log(
                        'Image converted to jpg and saved at ',
                        imagePath,
                      );
                    })
                    .catch(err => console.log(err));
                })
                .catch(e => console.log(e));
            })
            .catch((e: any) => console.log(e));
        }
      },
    };
    selectAuxActionButtons.push(takeScreenshot);
    selectRemoteActionButtons.push(takeScreenshot);
    selectLocalActionButtons.push(takeScreenshot);
  }

  const promptUser = () => {
    setAlertModalVisible(true);
  };

  return HmsView ? (
    <View key={id} style={[videoStyles(), speaking && styles.highlight]}>
      <AlertModal
        modalVisible={alertModalVisible}
        setModalVisible={setAlertModalVisible}
        title={selectActionTitle}
        message={selectActionMessage}
        buttons={
          type === 'screen'
            ? selectAuxActionButtons
            : type === 'local'
            ? selectLocalActionButtons
            : selectRemoteActionButtons
        }
      />
      <CustomModal
        modalVisible={volumeModal}
        setModalVisible={setVolumeModal}
        title={modalTitle}
        buttons={modalButtons}>
        <Slider
          value={volume}
          maximumValue={10}
          minimumValue={0}
          step={0.1}
          onValueChange={(value: any) => setVolume(value[0])}
        />
      </CustomModal>
      <CustomModal
        modalVisible={roleModalVisible}
        setModalVisible={setRoleModalVisible}
        title={roleRequestTitle}
        buttons={roleRequestButtons}>
        <RolePicker
          data={knownRoles}
          selectedItem={newRole}
          onItemSelected={setNewRole}
        />
      </CustomModal>
      {statsForNerds && (
        <View style={styles.statsContainer}>
          {type === 'local' ? (
            <View>
              <Text style={styles.statsText}>
                Bitrate(A) = {localAudioStats?.bitrate}
              </Text>
              <Text style={styles.statsText}>
                Bitrate(V) = {localVideoStats?.bitrate}
              </Text>
            </View>
          ) : (
            <View>
              <Text style={styles.statsText}>
                Bitrate(A) = {remoteAudioStats[id!]?.bitrate}
              </Text>
              <Text style={styles.statsText}>
                Bitrate(V) = {remoteVideoStats[id!]?.bitrate}
              </Text>
              <Text style={styles.statsText}>
                Jitter(A) = {remoteAudioStats[id!]?.jitter}
              </Text>
              <Text style={styles.statsText}>
                Jitter(V) = {remoteVideoStats[id!]?.jitter}
              </Text>
            </View>
          )}
        </View>
      )}
      {isVideoMute || layout === 'audio' ? (
        <View style={styles.avatarContainer}>
          <View style={[styles.avatar, {backgroundColor: colour}]}>
            <Text style={styles.avatarText}>{getInitials(name!)}</Text>
          </View>
        </View>
      ) : (
        <View style={styles.flex}>
          <HmsView
            ref={hmsViewRef}
            setZOrderMediaOverlay={miniView}
            trackId={trackId!}
            mirror={
              type === 'local' && mirrorLocalVideo !== undefined
                ? mirrorLocalVideo
                : false
            }
            scaleType={
              type === 'screen'
                ? HMSVideoViewMode.ASPECT_FIT
                : HMSVideoViewMode.ASPECT_FILL
            }
            style={type === 'screen' ? styles.hmsViewScreen : styles.hmsView}
          />
          {isDegraded && (
            <View style={styles.degradedContainer}>
              <View style={styles.avatarContainer}>
                <Text style={styles.degradedText}>Degraded</Text>
              </View>
            </View>
          )}
        </View>
      )}
      <View style={styles.labelContainer}>
        {peerReference?.networkQuality?.downlinkQuality &&
        peerReference?.networkQuality?.downlinkQuality > -1 ? (
          <View>
            <Image
              style={styles.network}
              source={
                peerReference?.networkQuality?.downlinkQuality === 0
                  ? require('../../assets/network_0.png')
                  : peerReference?.networkQuality?.downlinkQuality === 1
                  ? require('../../assets/network_1.png')
                  : peerReference?.networkQuality?.downlinkQuality === 2
                  ? require('../../assets/network_2.png')
                  : peerReference?.networkQuality?.downlinkQuality === 3
                  ? require('../../assets/network_3.png')
                  : require('../../assets/network_4.png')
              }
            />
          </View>
        ) : (
          <></>
        )}
        {metadata?.isHandRaised && (
          <View>
            <Ionicons
              name="ios-hand-left"
              style={styles.raiseHand}
              size={dimension.viewHeight(30)}
            />
          </View>
        )}
        {metadata?.isBRBOn && (
          <View>
            <View style={styles.brbOnContainer}>
              <Text style={styles.brbOn}>BRB</Text>
            </View>
          </View>
        )}
      </View>
      {type === 'screen' ||
      (type === 'local' && selectLocalActionButtons.length > 1) ||
      (type === 'remote' && selectRemoteActionButtons.length > 1) ? (
        <TouchableOpacity onPress={promptUser} style={styles.optionsContainer}>
          <Entypo
            name="dots-three-horizontal"
            style={styles.options}
            size={dimension.viewHeight(30)}
          />
        </TouchableOpacity>
      ) : (
        <></>
      )}
      <View style={styles.displayContainer}>
        <View style={styles.peerNameContainer}>
          <Text numberOfLines={2} style={styles.peerName}>
            {name}
          </Text>
        </View>
        <View style={styles.micContainer}>
          <Feather
            name={isAudioMute ? 'mic-off' : 'mic'}
            style={styles.mic}
            size={20}
          />
        </View>
        <View style={styles.micContainer}>
          <Feather
            name={isVideoMute ? 'video-off' : 'video'}
            style={styles.mic}
            size={20}
          />
        </View>
      </View>
    </View>
  ) : (
    <></>
  );
};

export {DisplayTrack};
