package no.nordicsemi.android.nrfthingy.dfu;

import android.content.Context;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import no.nordicsemi.android.nrfthingy.R;

public class DfuHelper {
    @RawRes static final int CURRENT_FW_ID = R.raw.thingy_dfu_pkg_app_v2_2_0;
    @RawRes static final int CURRENT_FW_FULL_ID = R.raw.thingy_dfu_sd_bl_app_v2_2_0;

    public static boolean isFirmwareUpdateAvailable(@NonNull final Context context, @NonNull final String currentVersion) {
        final String[] fwVersion = currentVersion.split("\\.");

        final int fwVersionMajor = Integer.parseInt(fwVersion[fwVersion.length - 3]);
        final int fwVersionMinor = Integer.parseInt(fwVersion[fwVersion.length - 2]);
        final int fwVersionPatch = Integer.parseInt(fwVersion[fwVersion.length - 1]);
        final String name = context.getResources().getResourceEntryName(CURRENT_FW_ID).replace("v", "");
        final String[] resourceEntryNames = name.split("_");

        final int fwFileVersionMajor = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 3]);
        final int fwFileVersionMinor = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 2]);
        final int fwFileVersionPatch = Integer.parseInt(resourceEntryNames[resourceEntryNames.length - 1]);

        if (fwFileVersionMajor > fwVersionMajor) {
            return true;
        } else if (fwFileVersionMajor == fwVersionMajor && fwFileVersionMinor > fwVersionMinor) {
            return true;
        } else if (fwFileVersionMajor == fwVersionMajor && fwFileVersionMinor == fwVersionMinor && fwFileVersionPatch > fwVersionPatch) {
            return true;
        }
        return false;
    }

    public static String getCurrentFwVersion(@NonNull final Context context) {
        final String name = context.getResources()
                .getResourceEntryName(CURRENT_FW_ID).replace("v", "");

        final String[] resourceEntryNames = name.split("_");
        return resourceEntryNames[resourceEntryNames.length - 3] + "." +
                resourceEntryNames[resourceEntryNames.length - 2] + "." +
                resourceEntryNames[resourceEntryNames.length - 1];
    }

    static String getCurrentFwFileName(@NonNull final Context context, final boolean withSdAndBl) {
        return context.getResources().getResourceEntryName(withSdAndBl ? CURRENT_FW_FULL_ID : CURRENT_FW_ID);
    }

    static InputStream getCurrentFwStream(@NonNull final Context context, final boolean withSdAndBl) {
        return context.getResources().openRawResource(withSdAndBl ? CURRENT_FW_FULL_ID : CURRENT_FW_ID);
    }
}
