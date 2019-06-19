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
package com.iven.libptp.commands.nikon;

import com.iven.libptp.NikonCamera;
import com.iven.libptp.PtpCamera.IO;
import com.iven.libptp.PtpConstants.Operation;
import com.iven.libptp.PtpConstants.Response;

import java.nio.ByteBuffer;

public class NikonAfDriveCommand extends NikonCommand {

    public NikonAfDriveCommand(NikonCamera camera) {
        super(camera);
    }

    @Override
    public void exec(IO io) {
        //        if (camera.isInActivationTypePhase()) {
        //            return;
        //        }
        io.handleCommand(this);
        if (getResponseCode() == Response.Ok) {
            camera.onFocusStarted();
            camera.enqueue(new NikonAfDriveDeviceReadyCommand(camera), 200);
        }
    }

    @Override
    public void encodeCommand(ByteBuffer b) {
        encodeCommand(b, Operation.NikonAfDrive);
    }
}
