package codecheck;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class App {

	public static String SEED;

	// API最適化用のMap
	public static Map<Integer, Integer> map = new HashMap<Integer, Integer>(){
		{
			put(0, 1);
			put(2, 2);
		}
	};

	public static void main(String[] args) {
		// 引数バリデーション
		if (args.length != 2) {
			System.out.println("");
			System.err.println("第一引数: API実行時に使用するseed文字列 および 第二引数: 再帰関数で結果を計算するn整数値 は必須入力です。");
			return;
		}

		try {
			SEED = args[0];
			int n = Integer.parseInt(args[1]);
			System.out.println(execute(n));
		} catch (NumberFormatException e1) {
			System.out.println("");
			System.err.println("第二引数: 再帰関数で結果を計算するn整数値 は整数値で入力して下さい。");
		} catch (Exception e2) {
			System.out.println("");
			System.err.println(e2.getMessage());
		}
	}

	/**
	 * メインとなる関数f(x)の処理
	 * @param n
	 * @return
	 * @throws Exception
	 */
	public static int execute(int n) throws Exception {
		if (map.containsKey(n)) {
			return map.get(n);
		}

		if (n % 2 == 0) {
			return execute(n - 1) + execute(n - 2) + execute(n - 3) + execute(n - 4);
		} else {
			int result = askServer(n);
			map.put(n, result);
			return result;
		}
	}

	/**
	 * recursive APIをコールして、計算結果を取得する
	 * @param n
	 * @return
	 * @throws Exception API実行上限に達した場合
	 */
	public static int askServer(int n) throws Exception {
		StringBuffer stringBuffer = new StringBuffer();
		String apiUrl = "http://challenge-server.code-check.io/api/recursive/ask";
		String requestParam = "?seed=" + SEED + "&n=" + n;

		try {
			URL url = new URL(apiUrl + requestParam);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

			httpURLConnection.setRequestMethod("GET");
			httpURLConnection.setDoOutput(true);
			httpURLConnection.setInstanceFollowRedirects(true);

			int code = httpURLConnection.getResponseCode();

			if (code == HttpURLConnection.HTTP_OK) {
				// 通信に成功した
				// テキストを取得する
				final InputStream in = httpURLConnection.getInputStream();
				String encoding = httpURLConnection.getContentEncoding();
				if(encoding == null){
					encoding = "UTF-8";
				}
				final InputStreamReader inReader = new InputStreamReader(in, encoding);
				final BufferedReader bufReader = new BufferedReader(inReader);
				String line = null;
				// 1行ずつテキストを読み込む
				while((line = bufReader.readLine()) != null) {
					stringBuffer.append(line);
				}
				bufReader.close();
				inReader.close();
				in.close();
				//System.out.println("API Response : " + stringBuffer);

				JsonObject json = new Gson().fromJson(stringBuffer.toString(), JsonObject.class);
				int result = json.get("result").getAsInt();

				//System.out.println("result : " + result);

				return result;
			} else {
				throw new Exception("API実行が規定回数(50回)を超えました。1時間以上間隔を開けて、再実行して下さい。");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}
}
