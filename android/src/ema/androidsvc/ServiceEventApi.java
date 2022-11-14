package ema.androidsvc;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

@Kroll.proxy()
public class ServiceEventApi extends KrollProxy  {
    public static final ServiceEventApi instance = new ServiceEventApi();

    @Kroll.method()
    public String fetchEvents() {
        return "[]";
    }
}
