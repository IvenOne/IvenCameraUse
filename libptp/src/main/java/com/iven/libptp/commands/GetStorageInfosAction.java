/**
 * Copyright 2013 Nils Assbeck, Guersel Ayaz and Michael Zoech
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iven.libptp.commands;

import com.iven.libptp.Camera;
import com.iven.libptp.Camera.StorageInfoListener;
import com.iven.libptp.PtpAction;
import com.iven.libptp.PtpCamera;
import com.iven.libptp.PtpCamera.IO;
import com.iven.libptp.PtpConstants.Response;

public class GetStorageInfosAction implements PtpAction {

    private final PtpCamera camera;
    private final StorageInfoListener listener;

    public GetStorageInfosAction(PtpCamera camera, Camera.StorageInfoListener listener) {
        this.camera = camera;
        this.listener = listener;
    }

    @Override
    public void exec(IO io) {
        GetStorageIdsCommand getStorageIds = new GetStorageIdsCommand(camera);
        io.handleCommand(getStorageIds);

        if (getStorageIds.getResponseCode() != Response.Ok) {
            listener.onAllStoragesFound();
            return;
        }

        int ids[] = getStorageIds.getStorageIds();
        for (int i = 0; i < ids.length; ++i) {
            int storageId = ids[i];
            GetStorageInfoCommand c = new GetStorageInfoCommand(camera, storageId);
            io.handleCommand(c);

            if (c.getResponseCode() != Response.Ok) {
                listener.onAllStoragesFound();
                return;
            }

            String label = c.getStorageInfo().volumeLabel.isEmpty() ? c.getStorageInfo().storageDescription : c
                    .getStorageInfo().volumeLabel;
            if (label == null || label.isEmpty()) {
                label = "Storage " + i;
            }
            listener.onStorageFound(storageId, label);
        }

        listener.onAllStoragesFound();
    }

    @Override
    public void reset() {
    }
}
