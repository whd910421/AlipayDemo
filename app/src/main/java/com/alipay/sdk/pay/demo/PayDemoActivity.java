package com.alipay.sdk.pay.demo;

import com.alipay.sdk.app.EnvUtils;
import com.alipay.sdk.auth.AlipaySDK;
import java.util.Map;

import com.alipay.sdk.app.AuthTask;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.demo.util.OrderInfoUtil2_0;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 *  重要说明:
 *  
 *  这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
 *  真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
 *  防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险； 
 */
public class PayDemoActivity extends FragmentActivity {
	
	/** 支付宝支付业务：入参app_id */
	public static final String APPID = "2016080200151565";
	
	/** 支付宝账户登录授权业务：入参pid值 */
	public static final String PID = "2088102169650873";
	/** 支付宝账户登录授权业务：入参target_id值 */
	public static final String TARGET_ID = "1";

	/** 商户私钥，pkcs8格式 */
	/** 如下私钥，RSA2_PRIVATE 或者 RSA_PRIVATE 只需要填入一个 */
	/** 如果商户两个都设置了，优先使用 RSA2_PRIVATE */
	/** RSA2_PRIVATE 可以保证商户交易在更加安全的环境下进行，建议使用 RSA2_PRIVATE */
	/** 获取 RSA2_PRIVATE，建议使用支付宝提供的公私钥生成工具生成， */
	/** 工具地址：https://doc.open.alipay.com/docs/doc.htm?treeId=291&articleId=106097&docType=1 */

	public static final String RSA2_PRIVATE = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKVXnCpicMykgQmV2BbpW6i4oRZ95dsC9gqIksGsgPtGPPDG3ZL2VventwCT81wQe16sdg2cwWTKluLgKYyGTXb2TDzWz+q+THiAxnphXNYk+BsYZyZNmnvFD5PYVOzE91EGMNH1IM8B0dfK5twPvtzDDa1SgSLPuSgRVGSHVlWxRJnxNJ897+gUWl2BygUAku2p41qmIwdJXtih4jvp48FYMcdpHc9VUJkM5zgGvh27f2uVnybHP+4cwcL+rAH16C1RL2HKa8gPiWmYJ7DFs82Z/SdrhMlhSwbhl/eW+gz7SKyXglyv6JZgZMgiupG+PfqKxw274K3fH3guxJB5shAgMBAAECggEAeN5i5GK5RSy5rCDR+PYEv/GHwyoXbkRHqShE+XHuPWN3lEJKhMmJgPTcNGtxtTzgzE26WSMChfe55zXZG0fq5b+HpGhdSi2Jd6DQOELBF3Y5JLLWLBQO6eLXRGz5676lYLsvULF7JHt0CR9oW+xDEyU9shxP2bvBdR1jhh7W8npLzCDxP/KTaklPquCOiFId6bMI4CW8Q6phqJQp7M5SBm+uuUOJV5Ixkd+5bYuo+HS8RhFsaCt0hZa9F6dGNakuRmeReQHO7cBv5aQEXyL+8Isw4WA+Jo92N8G06vx2liPzZXssQUVMy8fwOUvuNDKb0bwGFfPeOIlRY6YILlDTOQKBgQDyPLxN8vHDdtk5YMMwHqueu8HeX3BjguNDXLx5oBPUh1yr2n38zxx8TCsuK77x3B9IYc3ZQklDp9UkMSnAblZkdYVV48MrqSCGWuCvbG+16aWdWUSe5q2gXv79jbn92w4nFYBdurL5N2ZJy5fCUCIfCR7Nmxra5KV5Ys8Ob+1L/wKBgQDV1Fu4KiOarkVPXxDprQR1W5bofQFMpwLr7+F92YWgzm9jQ3kpBfopZA2n1L3WbhzeGQxJMCUX+24zFy+OynjSnJtr0/Wk+uzaWJKIQco1bvd5zjvlzOzbRcGFjpUFwhPhiirRBbDPPHEYeO/oUfKQF7Tv6jUmFFHodL51982Y3wKBgQCQbbwMcdTKKTt9JX9bWn4TB3mg/6CABJNCH7HUhqgsKIUNhhlmOYqPZc69GaIgjJEuU/orFoWeTBweFx4AuQjTnupf+ovspUtaMCcVEP9OJD/1oDr0zy8olltMjEsf25MFpKFDwURiqTBYwYX7elHN1V9DUb5iUr+Cr3Iq9nsyuwKBgHO8TSiRJlRCnyEmw3C3RQKAYF3Vyy5B90Lcw5/qbHltQrzneFzL72sXzIxaiaXWU6mixROYC3u/ixX8JrGSxXK5cSYbw4lYh3pnFmjJszjYpvo2thTbphfI2cYANoi3UytwLVyVR+3jOROV4cRNFo0HR9fTXJEPzoWTKsOPLKjhAoGASa4a3uDv6ZxzbM6W6bRFnQq5ECQkRFoIvUh84rHencwiOx3z7aUb0uLn+ldmA4PZVX8R2VfO8/O9XW6qpIe7H6SoxyaeVHJCWYTMzyYtPMgJ3paaIFWrrM8XUgnZlnMN+u3b1X86YwjzrSt67Fh87BEpdA9ZOWP63GsnSpnD8eE=" ;
  public static final String RSA_PRIVATE = "";
	
	private static final int SDK_PAY_FLAG = 1;
	private static final int SDK_AUTH_FLAG = 2;
  private static final String TAG = "gggggggggg";

  @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unused")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SDK_PAY_FLAG: {
				@SuppressWarnings("unchecked")
				PayResult payResult = new PayResult((Map<String, String>) msg.obj);
				/**
				 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
				 */
				String resultInfo = payResult.getResult();// 同步返回需要验证的信息
				String resultStatus = payResult.getResultStatus();
				// 判断resultStatus 为9000则代表支付成功
				if (TextUtils.equals(resultStatus, "9000")) {
					// 该笔订单是否真实支付成功，需要依赖服务端的异步通知。
					Toast.makeText(PayDemoActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
				} else {
					// 该笔订单真实的支付结果，需要依赖服务端的异步通知。
					Toast.makeText(PayDemoActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case SDK_AUTH_FLAG: {
				@SuppressWarnings("unchecked")
				AuthResult authResult = new AuthResult((Map<String, String>) msg.obj, true);
				String resultStatus = authResult.getResultStatus();

				// 判断resultStatus 为“9000”且result_code
				// 为“200”则代表授权成功，具体状态码代表含义可参考授权接口文档
				if (TextUtils.equals(resultStatus, "9000") && TextUtils.equals(authResult.getResultCode(), "200")) {
					// 获取alipay_open_id，调支付时作为参数extern_token 的value
					// 传入，则支付账户为该授权账户
					Toast.makeText(PayDemoActivity.this,
							"授权成功\n" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT)
							.show();
				} else {
					// 其他状态值则为授权失败
					Toast.makeText(PayDemoActivity.this,
							"授权失败" + String.format("authCode:%s", authResult.getAuthCode()), Toast.LENGTH_SHORT).show();

				}
				break;
			}
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay_main);
	}
	
	/**
	 * 支付宝支付业务
	 * 
	 * @param v
	 */
	public void payV2(View v) {
		if (TextUtils.isEmpty(APPID) || (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))) {
			new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置APPID | RSA_PRIVATE")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i) {
							//
							finish();
						}
					}).show();
			return;
		}
    EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
		/**
		 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
		 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
		 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险； 
		 * 
		 * orderInfo的获取必须来自服务端；
		 */
        boolean rsa2 = (RSA2_PRIVATE.length() > 0);
		Map<String, String> params = OrderInfoUtil2_0.buildOrderParamMap(APPID, rsa2);
		String orderParam = OrderInfoUtil2_0.buildOrderParam(params);

		String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
		String sign = OrderInfoUtil2_0.getSign(params, privateKey, rsa2);
		final String orderInfo = orderParam + "&" + sign;
		
		//Runnable payRunnable = new Runnable() {
    //
		//	@Override
		//	public void run() {
		//		PayTask alipay = new PayTask(PayDemoActivity.this);
		//		Map<String, String> result = alipay.payV2(orderInfo, true);
		//		Log.i("msp", result.toString());
    //
		//		Message msg = new Message();
		//		msg.what = SDK_PAY_FLAG;
		//		msg.obj = result;
		//		mHandler.sendMessage(msg);
		//	}
		//};
    //
    //Thread payThread = new Thread(payRunnable);
    //payThread.start();
    Observable.create(new Observable.OnSubscribe<PayResult>() {
      @Override public void call(Subscriber<? super PayResult> subscriber) {
        PayTask alipay = new PayTask(PayDemoActivity.this);
        Map<String, String> payResult = alipay.payV2(orderInfo, true);
        subscriber.onNext(new PayResult(payResult));
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
        new Action1<PayResult>() {
          @Override public void call(PayResult payResult) {
            Log.i(TAG, "call: "+payResult.getResultStatus());
          }
        });
    Log.d("ssss","hhhhhhhhhhhhh");
	}

	/**
	 * 支付宝账户授权业务
	 * 
	 * @param v
	 */
	public void authV2(View v) {
		if (TextUtils.isEmpty(PID) || TextUtils.isEmpty(APPID)
				|| (TextUtils.isEmpty(RSA2_PRIVATE) && TextUtils.isEmpty(RSA_PRIVATE))
				|| TextUtils.isEmpty(TARGET_ID)) {
			new AlertDialog.Builder(this).setTitle("警告").setMessage("需要配置PARTNER |APP_ID| RSA_PRIVATE| TARGET_ID")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialoginterface, int i) {
						}
					}).show();
			return;
		}

		/**
		 * 这里只是为了方便直接向商户展示支付宝的整个支付流程；所以Demo中加签过程直接放在客户端完成；
		 * 真实App里，privateKey等数据严禁放在客户端，加签过程务必要放在服务端完成；
		 * 防止商户私密数据泄露，造成不必要的资金损失，及面临各种安全风险； 
		 * 
		 * authInfo的获取必须来自服务端；
		 */
		boolean rsa2 = (RSA2_PRIVATE.length() > 0);
		Map<String, String> authInfoMap = OrderInfoUtil2_0.buildAuthInfoMap(PID, APPID, TARGET_ID, rsa2);
		String info = OrderInfoUtil2_0.buildOrderParam(authInfoMap);
		
		String privateKey = rsa2 ? RSA2_PRIVATE : RSA_PRIVATE;
		String sign = OrderInfoUtil2_0.getSign(authInfoMap, privateKey, rsa2);
		final String authInfo = info + "&" + sign;
		Runnable authRunnable = new Runnable() {

			@Override
			public void run() {
				// 构造AuthTask 对象
				AuthTask authTask = new AuthTask(PayDemoActivity.this);
				// 调用授权接口，获取授权结果
				Map<String, String> result = authTask.authV2(authInfo, true);

				Message msg = new Message();
				msg.what = SDK_AUTH_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);
			}
		};

		// 必须异步调用
		Thread authThread = new Thread(authRunnable);
		authThread.start();
	}
	
	/**
	 * get the sdk version. 获取SDK版本号
	 * 
	 */
	public void getSDKVersion() {
		PayTask payTask = new PayTask(this);
		String version = payTask.getVersion();
		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 原生的H5（手机网页版支付切natvie支付） 【对应页面网页支付按钮】
	 * 
	 * @param v
	 */
	public void h5Pay(View v) {
		Intent intent = new Intent(this, H5PayDemoActivity.class);
		Bundle extras = new Bundle();
		/**
		 * url是测试的网站，在app内部打开页面是基于webview打开的，demo中的webview是H5PayDemoActivity，
		 * demo中拦截url进行支付的逻辑是在H5PayDemoActivity中shouldOverrideUrlLoading方法实现，
		 * 商户可以根据自己的需求来实现
		 */
		String url = "http://m.taobao.com";
		// url可以是一号店或者淘宝等第三方的购物wap站点，在该网站的支付过程中，支付宝sdk完成拦截支付
		extras.putString("url", url);
		intent.putExtras(extras);
		startActivity(intent);
	}

}
