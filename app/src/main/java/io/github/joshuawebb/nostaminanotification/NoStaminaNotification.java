package io.github.joshuawebb.nostaminanotification;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class NoStaminaNotification implements IXposedHookLoadPackage {

	private final static String targetPackage = "com.android.systemui";

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
		if (!targetPackage.equals(lpparam.packageName))
			return;

		XposedBridge.log("load package NoStaminaNotification");

		String targetClassName = targetPackage + ".power.PowerNotificationWarnings";
		final Class<?> notifierClass = XposedHelpers.findClass(targetClassName, lpparam.classLoader);

		// set saver mode to be off according to the Notification Generator
		//  ~ not the PowerManager.
		XposedHelpers.findAndHookMethod(notifierClass, "showSaverMode", "boolean",
			new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					super.beforeHookedMethod(param);
					XposedBridge.log(param.method.getName() + " called");

					// boolean : mode
					param.args[0] = false;
				}
			}
		);
	}
}
