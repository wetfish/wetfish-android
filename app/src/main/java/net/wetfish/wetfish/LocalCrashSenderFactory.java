package net.wetfish.wetfish;

import android.content.Context;

import androidx.annotation.NonNull;

import org.acra.config.CoreConfiguration;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderFactory;

/**
 * Created by ${Michael} on 5/3/2019.
 */
public class LocalCrashSenderFactory implements ReportSenderFactory {
    /**
     * @param context a context.
     * @param config  Configuration to use when sending reports.
     * @return Fully configured instance of the relevant ReportSender.
     */
    @NonNull
    @Override
    public ReportSender create(@NonNull Context context, @NonNull CoreConfiguration config) {
        return new LocalCrashSender();
    }

    /**
     * controls if this instance is active
     *
     * @param config the current config
     * @return if this instance should be called
     */
    @Override
    public boolean enabled(@NonNull CoreConfiguration config) {
        return true;
    }
}
