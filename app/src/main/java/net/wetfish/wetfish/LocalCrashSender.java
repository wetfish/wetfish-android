package net.wetfish.wetfish;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import org.acra.ACRAConstants;
import org.acra.ReportField;
import org.acra.data.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ${Michael} on 5/2/2019.
 */
public class LocalCrashSender implements ReportSender {

    private static final String FILE_NAME = "AcraCrashReport.txt";

    public LocalCrashSender() {

    }

    /**
     * Send crash report data.
     * <p>
     * Method will be called from the {@link SenderService}.
     *
     * @param context      Android Context in which to send the crash report.
     * @param report Stores key/value pairs for each report field.
     * @throws ReportSenderException If anything goes fatally wrong during the handling of crash data, you can (should) throw a {@link ReportSenderException} with a custom message.
     */
    @Override
    public void send(@NonNull Context context, @NonNull CrashReportData  report) throws ReportSenderException {
        // Iterate over the CrashReportData instance and do whatever
        // you need with each pair of ReportField key / String value

        String finalReport = createCrashReport(report);
//        String tempFile = context.getFilesDir().getPath() + "/" + FILE_NAME;

        String tempFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + FILE_NAME;

        try
        {
            File detailedFile = new File(tempFile);

            if(!detailedFile.exists())
                detailedFile.createNewFile();

            FileOutputStream stream = new FileOutputStream(detailedFile, true);

            stream.write(finalReport.getBytes());
            Log.d("testAcra","adding to file: "+stream);
            stream.close();

        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }

    private String createCrashReport(CrashReportData crashReportData){
        StringBuilder body = new StringBuilder();

        body.append("ReportID : " + crashReportData.get(String.valueOf(ReportField.REPORT_ID)))
                .append("\n")
                .append("DeviceID : " + crashReportData.get(String.valueOf(ReportField.DEVICE_ID)))
                .append("\n")
                .append("AppVersionName : " + crashReportData.get(String.valueOf(ReportField.APP_VERSION_NAME)))
                .append("\n")
                .append("Android Version : " + crashReportData.get(String.valueOf(ReportField.ANDROID_VERSION)))
                .append("\n")
                .append("CustomData : " + crashReportData.get(String.valueOf(ReportField.CUSTOM_DATA)))
                .append("\n")
                .append("STACK TRACE : \n" + crashReportData.get(String.valueOf(ReportField.STACK_TRACE)))
                .append("\n")
                .append("LogCAT : \n" + crashReportData.get(String.valueOf(ReportField.LOGCAT)));

        return body.toString();
    }

    private boolean isNull(String aString) {
        return aString == null || ACRAConstants.NULL_VALUE.equals(aString);
    }
}

