package com.jinx.aspect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.jinx.annotation.Permissions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Aspect
public class PermissionAspect {

    @Pointcut("execution(@com.jinx.annotation.Permissions * *(..))")//com.jinx.annotation.Permissions
    public void methodAnnotated() {

    }

    @Around("methodAnnotated()")
    public void aroundJoinPoint(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Permissions annotation = method.getAnnotation(Permissions.class);
            if (annotation == null || annotation.permissions().length == 0) {
                //没有要申请的权限,切入点
                proceed(joinPoint);
                return;
            }
            //用户获取当前活动
            List<Activity> activities = getAllActivitys();
            if (activities.get(0) instanceof FragmentActivity) {
                //找不到栈顶activity
                FragmentActivity currentActivity = (FragmentActivity) activities.get(0);
                //创建权限请求工具
                RxPermissions rxPermissions = new RxPermissions(currentActivity);
                List<String> needPermission = new ArrayList<>();
                //过滤已申请的权限
                for (String permission : annotation.permissions()){
                    if (!rxPermissions.isGranted(permission)){
                        needPermission.add(permission);
                    }
                }
                if (needPermission.isEmpty()){
                    //权限都已授权
                    proceed(joinPoint);
                    return;
                }
                requestPermissions(currentActivity,rxPermissions,needPermission,joinPoint,annotation);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("all")
    private void requestPermissions(FragmentActivity activity, RxPermissions rxPermission, List<String> needPermission, ProceedingJoinPoint joinPoint, Permissions annotation) {
        //请求权限全部结果
        rxPermission.request(needPermission.toArray(new String[needPermission.size()]))
                .subscribe(granted -> {
                    if (granted) {
                        //全部授权了
                        Toast.makeText(activity, "权限都有了，愉快的玩耍吧", Toast.LENGTH_LONG).show();
                        proceed(joinPoint);
                    } else {
                        //有权限不通过
                        if (annotation.ignoreResult()) {
                            //忽略
                            Toast.makeText(activity, "有权限不通过,无所谓，继续愉快的玩耍", Toast.LENGTH_LONG).show();
                            proceed(joinPoint);
                        } else {
                            Toast.makeText(activity, "有权限不通过，不能愉快的玩耍了", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void proceed(ProceedingJoinPoint joinPoint) {
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * 通过反射获取activity堆栈
     * @return
     */
    public List<Activity> getAllActivitys() {
        List<Activity> list = new ArrayList<>();
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            //获取主线程对象
            Object activityThreadObject = currentActivityThread.invoke(null);
            Field mActivitiesField = activityThread.getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Map<Object, Object> mActivities = (Map<Object, Object>) mActivitiesField.get(activityThreadObject);
            for (Map.Entry<Object, Object> entry : mActivities.entrySet()) {
                Object value = entry.getValue();
                Class<?> activityClientRecordClass = value.getClass();
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Object o = activityField.get(value);
                list.add((Activity) o);
                Log.d(((Activity) o).getLocalClassName(), ((Activity) o).getLocalClassName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

}
