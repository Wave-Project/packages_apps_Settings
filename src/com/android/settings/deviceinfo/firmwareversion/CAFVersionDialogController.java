/*
 * Copyright (C) 2019 - WaveProject
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.os.SystemProperties;

import com.android.settings.R;

public class CAFVersionDialogController {
    private static final int CAF_VERSION_VALUE_ID = R.id.caf_version_value;
    private static final int CAF_VERSION_LABEL_ID = R.id.caf_version_label;
    private static final String CAF_PROPERTY = "ro.qti.caf.version";

    private final FirmwareVersionDialogFragment mDialog;
    private final Context mContext;

    public CAFVersionDialogController(FirmwareVersionDialogFragment dialog) {
        mDialog = dialog;
        mContext = dialog.getContext();
    }

    public void initialize() {
        mDialog.setText(CAF_VERSION_VALUE_ID, SystemProperties.get(CAF_PROPERTY,
                mContext.getResources().getString(R.string.device_info_default)));
    }
}
