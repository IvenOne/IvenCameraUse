
package com.iven.libptp;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;

import com.iven.libptp.model.LiveViewData;

import java.util.Collection;

public class PtpUsbService {

    private final String ACTION_USB_PERMISSION = "com.lunan.cloudpicture.Request.USB_PERMISSION";
    private Context mContext;
    private PtpUsbServiceInterface mInterface;

    public interface PtpUsbServiceInterface {
        void error(String msg);

        void notifyCameraCanConnect(MtpDevice mMtpDevice);
    }


    public PtpUsbService(Context mContext, PtpUsbServiceInterface mInterface) {
        this.mContext = mContext;
        this.mInterface = mInterface;
        registerReceiverMtp();
        getMtpService();

    }

    /**
     * 广播监听
     *
     * @author Iven
     * @date 2019-06-19
     */
    private BroadcastReceiver mtpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            switch (data.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    getMtpService();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
//                    ToastUtil.showToast("USB设备已断开！");
                    mInterface.error("USB设备已断开！");
                    break;
                case ACTION_USB_PERMISSION:
                    if (data.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        getMtpService();
                    }
                    break;
            }
        }
    };


    public void getMtpService() {
        //检测连接的USB设备
        final UsbManager mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        Collection<UsbDevice> mUsbDevices = mUsbManager.getDeviceList().values();

        //不可连接多个USB设备
        if (mUsbDevices.size() == 0) {
//            ToastUtil.showToast("未检测到相机！");
            mInterface.error("未检测到相机！");
            return;
        }

        //获取USB设备对象
        final UsbDevice mUsbDevice = mUsbDevices.iterator().next();

        //尝试建立MTP连接
        if (mUsbManager.hasPermission(mUsbDevice)) {
            for (int i = 0, n = mUsbDevice.getInterfaceCount(); i < n; ++i) {
                UsbInterface intf = mUsbDevice.getInterface(i);
                if (intf.getEndpointCount() != 3) {
                    continue;
                }
                UsbEndpoint in = null;
                UsbEndpoint out = null;

                for (int e = 0, en = intf.getEndpointCount(); e < en; ++e) {
                    UsbEndpoint endpoint = intf.getEndpoint(e);
                    if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                            in = endpoint;
                        } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                            out = endpoint;
                        }
                    }
                }

                if (in == null || out == null) {
                    continue;
                }

                final PtpUsbConnection connection = new PtpUsbConnection(mUsbManager.openDevice(mUsbDevice), in, out, mUsbDevice.getVendorId(), mUsbDevice.getProductId());
                if (connection.getVendorId() == PtpConstants.CanonVendorId) {
                    new EosCamera(connection, new Camera.CameraListener() {
                        @Override
                        public void onCameraStarted(final Camera camera) {
                            camera.setCapturedPictureSampleSize(2);
                            camera.retrieveStorages(new Camera.StorageInfoListener() {

                                @Override
                                public void onStorageFound(int handle, String label) {
//                                    LLog.d("mStorages", "onStorageFound");
                                }

                                @Override
                                public void onAllStoragesFound() {
//                                    LLog.d("mStorages", "onAllStoragesFound");


                                    //打开USB连接
                                    UsbDeviceConnection mConnection = mUsbManager.openDevice(mUsbDevice);
                                    //建立Mtp连接
                                    MtpDevice mMtpDevice = new MtpDevice(mUsbDevice);
                                    boolean isOpenMtp = mMtpDevice.open(mConnection);
                                    if (isOpenMtp) {
                                        mInterface.notifyCameraCanConnect(mMtpDevice);
                                    } else {
                                        mInterface.error("连接失败");
                                    }
                                    ((EosCamera) camera).shutdown();

                                }

                                @Override
                                public void onImageHandlesRetrieved(int[] handles) {
//                                    LLog.d("mStorages", "onImageHandlesRetrieved");
                                }
                            });

//                            LLog.d("mCamera", "onCameraStarted");
                        }

                        @Override
                        public void onCameraStopped(Camera camera) {
//                            LLog.d("mCamera", "onCameraStopped");
                        }

                        @Override
                        public void onNoCameraFound() {
//                            LLog.d("mCamera", "onNoCameraFound");
                        }

                        @Override
                        public void onError(String message) {
//                            LLog.d("mCamera", "onError");
                            if (message.contains("")) {

                            }
                        }

                        @Override
                        public void onPropertyChanged(int property, int value) {
//                            LLog.d("mCamera", "onPropertyChanged");
                        }

                        @Override
                        public void onPropertyStateChanged(int property, boolean enabled) {
//                            LLog.d("mCamera", "onPropertyStateChanged");
                        }

                        @Override
                        public void onPropertyDescChanged(int property, int[] values) {
//                            LLog.d("mCamera", "onPropertyDescChanged");
                        }

                        @Override
                        public void onLiveViewStarted() {
//                            LLog.d("mCamera", "onLiveViewStarted");
                        }

                        @Override
                        public void onLiveViewData(LiveViewData data) {
//                            LLog.d("mCamera", "onLiveViewData");
                        }

                        @Override
                        public void onLiveViewStopped() {
//                            LLog.d("mCamera", "onLiveViewStopped");
                        }

                        @Override
                        public void onCapturedPictureReceived(int objectHandle, String filename, Bitmap thumbnail, Bitmap bitmap) {
//                            LLog.d("mCamera", "onCapturedPictureReceived");
                        }

                        @Override
                        public void onBulbStarted() {
//                            LLog.d("mCamera", "onBulbStarted");
                        }

                        @Override
                        public void onBulbExposureTime(int seconds) {
//                            LLog.d("mCamera", "onBulbExposureTime");
                        }

                        @Override
                        public void onBulbStopped() {
//                            LLog.d("mCamera", "onBulbStopped");
                        }

                        @Override
                        public void onFocusStarted() {
//                            LLog.d("mCamera", "onFocusStarted");
                        }

                        @Override
                        public void onFocusEnded(boolean hasFocused) {
//                            LLog.d("mCamera", "onFocusEnded");
                        }

                        @Override
                        public void onFocusPointsChanged() {
//                            LLog.d("mCamera", "onFocusPointsChanged");
                        }

                        @Override
                        public void onObjectAdded(int handle, int format) {
//                            LLog.d("mCamera", "onObjectAdded");
                        }
                    }, new WorkerNotifier(mContext));
                } else {
                    //打开USB连接
                    UsbDeviceConnection mConnection = mUsbManager.openDevice(mUsbDevice);
                    //建立Mtp连接
                    MtpDevice mMtpDevice = new MtpDevice(mUsbDevice);
                    boolean isOpenMtp = mMtpDevice.open(mConnection);
                    if (isOpenMtp) {
                        mInterface.notifyCameraCanConnect(mMtpDevice);
                    } else {
                        mInterface.error("连接失败");
                    }
                }

            }

        } else {
            //没有权限申请权限
            PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
            mUsbManager.requestPermission(mUsbDevice, mPermissionIntent);
            return;
        }
    }

    /**
     * 注册广播
     *
     * @author Iven
     * @date 2019-06-19
     */
    private void registerReceiverMtp() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        mContext.registerReceiver(mtpReceiver, intentFilter);
    }

    public void unRegisterReceiverMtp() {
        mContext.unregisterReceiver(mtpReceiver);
    }
}
