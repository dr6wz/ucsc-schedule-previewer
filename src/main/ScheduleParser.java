package main;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import com.google.gson.*;
import com.google.gson.internal.LinkedTreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class ScheduleParser {

	public static void main(String[] args) {
		//		if (args.length != 1) {
		//			System.out.println("Too few arguments.");
		//			return;
		//		}
		//		System.out.println("building client");
		HttpClient client = HttpClient.newBuilder().build();
		//		System.out.println("client built");
		String subject = "";
		java.util.Scanner console = new java.util.Scanner(System.in);
		System.out.print("Subject: ");
		subject = console.nextLine();
		System.out.println();
		while (!subject.toLowerCase().equals("stop")) {
			runSearch(client, subject.toUpperCase());
			System.out.println();
			System.out.print("Subject: ");
			subject = console.nextLine();
			System.out.println();
		}
		console.close();
	}

	private static void runSearch(HttpClient client, String sub) {
		try {
//			System.out.println("building uri");
			String uri = "https://my.ucsc.edu/PSIGW/RESTListeningConnector/PSFT_CSPRD/SCX_CLASS_LIST.v1/2270?subject=" + sub.toUpperCase();
//			System.out.println("uri built");
//			System.out.println("building request");
			HttpRequest.Builder reqBuilder = HttpRequest.newBuilder(new URI(uri));
//			System.out.println("request builder built");
			reqBuilder.method("GET", BodyPublishers.ofString(""));
//			System.out.println("request method set");
			HttpRequest req = reqBuilder.build();
//			System.out.println("request built");
			HttpResponse.BodyHandler<String> responseHandler = BodyHandlers.ofString();
//			System.out.println("sending request");
			HttpResponse<String> response = client.send(req, responseHandler);
			if (response.statusCode() != 200) {
				System.out.println("HTTP " + response.statusCode());
				return;
			}
//			System.out.println("response received");
			String responseBody = response.body();
//			System.out.println("response extracted");
//			System.out.println(responseBody);

			Gson gson = new Gson();
			Type listType = new TypeToken<Map<String, ArrayList<Map<String, Object>>>>(){}.getType();
//			System.out.println("parsing response");
			Map<String, ArrayList<Map<String, Object>>> responseObj = gson.fromJson(responseBody, listType);
//			System.out.println("response parsed");
			ArrayList<Map<String, Object>> classes = responseObj.get("classes");
			// creates a parallel list with just the course numbers
			ArrayList<CourseNumber> numbers = new ArrayList<CourseNumber>();
			for (Map<String, Object> m: classes) {
				String num = (String) m.get("catalog_nbr");
				CourseNumber cn = new CourseNumber(num);
				numbers.add(cn);
			}
			// creates separate lists for lower division, upper division and graduate courses
			ArrayList<Map<String, Object>> lowerDivision = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> upperDivision = new ArrayList<Map<String, Object>>();
			ArrayList<Map<String, Object>> graduate = new ArrayList<Map<String, Object>>();
			// creates separate lists for course numbers of each category
			ArrayList<CourseNumber> ldNums = new ArrayList<CourseNumber>();
			ArrayList<CourseNumber> udNums = new ArrayList<CourseNumber>();
			ArrayList<CourseNumber> gradNums = new ArrayList<CourseNumber>();
			
			// sorts the list of classes
			ArrayList<Map<String, Object>> classesSorted = mergeSort(classes);
			
			// places each class into a bucket based on its level
			for (int a = 0; a < classesSorted.size(); a += 1) {
				Map<String, Object> ucscClass = classesSorted.get(a);
				CourseNumber cn = new CourseNumber((String) ucscClass.get("catalog_nbr"));
				if (cn.getType() == CourseNumber.LOWER_DIVISION) {
					lowerDivision.add(ucscClass);
				} else if (cn.getType() == CourseNumber.UPPER_DIVISION) {
					upperDivision.add(ucscClass);
				} else {
					graduate.add(ucscClass);
				}
			}
			
			// prints info for every class
			System.out.println("Lower Division:");
			for (Map<String, Object> m: lowerDivision) {
				@SuppressWarnings("unchecked")
				ArrayList<Map<String, Object>> instructors = (ArrayList<Map<String, Object>>) m.get("instructors");
				String number = (String) m.get("catalog_nbr");
				String subject = (String) m.get("subject");
				System.out.print(subject + " " + number + ", ");
				for (Map<String, Object> n: instructors) {
					System.out.print(n.get("name") + " ");
				}
				System.out.print(" | ");
				System.out.print(m.get("start_time") + "-" + m.get("end_time") + " " + m.get("meeting_days"));
				System.out.println();
			}
			
			System.out.println("\nUpper Division:");
			for (Map<String, Object> m: upperDivision) {
				@SuppressWarnings("unchecked")
				ArrayList<Map<String, Object>> instructors = (ArrayList<Map<String, Object>>) m.get("instructors");
				String number = (String) m.get("catalog_nbr");
				String subject = (String) m.get("subject");
				System.out.print(subject + " " + number + ", ");
				for (Map<String, Object> n: instructors) {
					System.out.print(n.get("name") + " ");
				}
				System.out.print(" | ");
				System.out.print(m.get("start_time") + "-" + m.get("end_time") + " " + m.get("meeting_days"));
				System.out.println();
			}
			
			System.out.println("\nGraduate:");
			for (Map<String, Object> m: graduate) {
				@SuppressWarnings("unchecked")
				ArrayList<Map<String, Object>> instructors = (ArrayList<Map<String, Object>>) m.get("instructors");
				String number = (String) m.get("catalog_nbr");
				String subject = (String) m.get("subject");
				System.out.print(subject + " " + number + ", ");
				for (Map<String, Object> n: instructors) {
					System.out.print(n.get("name") + " ");
				}
				System.out.print(" | ");
				System.out.print(m.get("start_time") + "-" + m.get("end_time") + " " + m.get("meeting_days"));
				System.out.println();
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * sorts a list of classes and its associated parallel list of course numbers using merge sort
	 * @precondition those lists must actually be parallel lists
	 * @param classes
	 * @param numbers
	 */
	private static ArrayList<Map<String, Object>> mergeSort(ArrayList<Map<String, Object>> classes) {
		if (classes.size() <= 1) {
			return classes;
		}
		
		int size = classes.size();
		int half = size / 2;
		ArrayList<Map<String, Object>> list1 = mergeSort(subList(classes, 0, half));
		ArrayList<Map<String, Object>> list2 = mergeSort(subList(classes, half, size));
		
		return merge(list1, list2);
	}
	
	private static ArrayList<Map<String, Object>> merge(ArrayList<Map<String, Object>> list1, ArrayList<Map<String, Object>> list2) {
		ArrayList<Map<String, Object>> c = new ArrayList<Map<String, Object>>();
		
		while (list1.size() != 0 && list2.size() != 0) {
			Map<String, Object> c1 = list1.get(0);
			Map<String, Object> c2 = list2.get(0);
			
			CourseNumber n1 = new CourseNumber((String) c1.get("catalog_nbr"));
			CourseNumber n2 = new CourseNumber((String) c2.get("catalog_nbr"));
			
			if (n1.compareTo(n2) > 0) {
				c.add(list2.remove(0));
			} else {
				c.add(list1.remove(0));
			}
		}
		
		while (list1.size() != 0) {
			c.add(list1.remove(0));
		}
		
		while (list2.size() != 0) {
			c.add(list2.remove(0));
		}
		
		return c;
	}
	
	private static ArrayList<Map<String, Object>> subList(ArrayList<Map<String, Object>> list, int begin, int end) {
		ArrayList<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (int a = begin; a < end; a += 1) {
			newList.add(list.get(a));
		}
		return newList;
	}
}
