package io.github.joshuawebb.nostaminanotification;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

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

		for (Field f : notifierClass.getDeclaredFields()) {
			XposedBridge.log("Field: " + f.getName() + " | " + Modifier.isStatic(f.getModifiers()) + " | " + f.isAccessible());
		}

		final Field notificationManager = findField(notifierClass, "mNoMan");
		if (notificationManager == null) {
			XposedBridge.log("notification manager not found");
			return;
		}

		final Field showingStringsField = findField(notifierClass, "SHOWING_STRINGS");
		if (showingStringsField == null) {
			XposedBridge.log("showingStrings not found");
			return;
		}

		notificationManager.setAccessible(true);

		showingStringsField.setAccessible(true);
		String[] showingStrings = (String[])showingStringsField.get(null);
		for(String s : showingStrings) {
			XposedBridge.log("SHOWING_STRING: " + s);
		}

		XC_MethodHook blockCallHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				super.beforeHookedMethod(param);
				XposedBridge.log(param.method.getName() + ": before hook!");
				param.setResult(null);
			}
		};

		for(String method : new String[] {
			"showCriticalNotification",
			"showWarningNotification",
			"showInvalidChargerNotification",
		})
		{
			try {
				XposedHelpers.findAndHookMethod(notifierClass, method, blockCallHook);
			} catch (Throwable t) {
				XposedBridge.log("findAndHook '"+ method +"' failed: " + t.getMessage() + t);
			}
		}

		XposedHelpers.findAndHookMethod(notifierClass, "showSaverMode",
			new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
					super.beforeHookedMethod(param);
					XposedBridge.log(param.method.getName() + " called");
					// set saver mode to be off...
					param.args[0] = false;
				}
			}
		);
	}

	private Field findField(Class<?> clazz, String fieldName)
	{
		try {
			Field notificationManager = clazz.getDeclaredField(fieldName);
			XposedBridge.log("Direct lookup success");
			return notificationManager;
		}
		catch (Throwable t) {
			XposedBridge.log("Direct lookup failed");
		}

		for (Field f : clazz.getDeclaredFields()) {
			if (fieldName.equals(f.getName())) {
				XposedBridge.log("Indirect lookup succeeded");
				return f;
			}
		}

		return null;
	}
}
