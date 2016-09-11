package io.github.joshuawebb.quietxperiastaminamode;

import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoStaminaNotification implements IXposedHookLoadPackage {
	private static final String TAG = NoStaminaNotification.class.getName();
	private final static String targetPackage = "com.sonymobile.superstamina";

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!targetPackage.equals(lpparam.packageName))
			return;

		String targetClassName = targetPackage + ".XperiaPowerService";
		Class<?> xperiaPowerService = XposedHelpers.findClass(targetClassName, lpparam.classLoader);

		XposedHelpers.findAndHookMethod(xperiaPowerService, "updateNotification", "boolean",
		  new XC_MethodHook() {
			  @Override
			  protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				  super.beforeHookedMethod(param);
				  Log.i(TAG, param.method.getName() + ": before hook | arg: " + param.args[0]);
				  // always turn OFF the notification
				  param.args[0] = false;
			  }
		  }
		);
	}
}
