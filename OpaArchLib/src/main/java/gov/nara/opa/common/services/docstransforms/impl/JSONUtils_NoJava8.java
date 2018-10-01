package gov.nara.opa.common.services.docstransforms.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONUtils_NoJava8 {
	
	public interface FakeConsumer<T> {
		public void accept(T t);
	}
	/**
	 * 
	 * @param parent
	 * @param primitiveOnly
	 *            if true return only terminated endpoints aka those ending with
	 *            a primitive. So don't return all the parents.
	 * @return
	 */

	public static List<PathValue> getEndPointsList(Object parent, boolean primitiveOnly) {
		final List<PathValue> list = new ArrayList<PathValue>();

		// Consumer<PathValue> consumer = x -> {
		// if (primitiveOnly) {
		// boolean bad = isJSONObject(x.value) || isJSONArray(x.value);
		// if (bad) {
		// return;
		// }
		// }
		// list.add(x);
		// };
		class FakeConsumerI implements FakeConsumer<PathValue> {
			private final boolean primitiveOnly;

			public FakeConsumerI(boolean aprimitveOnly) {
				primitiveOnly = aprimitveOnly;
			}

			@Override
			public void accept(PathValue pv) {
				if (primitiveOnly) {
					boolean bad = isJSONObject(pv.value) || isJSONArray(pv.value);
					if (bad) {
						return;
					}
				}
				list.add(pv);
			}
		}
		FakeConsumerI consumer = new FakeConsumerI(primitiveOnly);
		Stack<Object> s = new Stack<Object>();
		s.push("ROOT");
		recurse(parent, s, consumer);
		return list;
	}

	public static class PathValueOrderComparator implements Comparator<PathValue> {
		int order = 1;

		public PathValueOrderComparator(boolean orderAscending) {
			order = orderAscending ? 1 : -1;
		}

		@Override
		public int compare(PathValue o1, PathValue o2) {

			return order * Integer.compare(o1.index, o2.index);
		}

	}

	public static class PathValue {
		private int index = -1;
		private final Stack<Object> path;
		private final Object value;

		public PathValue(Stack<Object> apath, Object avalue) {
			path = new Stack<Object>();

			for(int i=0;i<apath.size();i++){
				path.push(apath.get(i));
			}
			value = avalue;
		}

		public Stack<Object> getPath() {
			return path;
		}
		public String getPathAsString(){
			return stackToStringPath(path, false);
		}
		public Object getValue() {
			return value;
		}

		public int getIndex() {
			return index;
		}

		public String toString() {
			return stackToStringPath(path, false);
		}
	}

	/**
	 * 
	 * @param stack
	 * @param includeROOT
	 *            - if true and the zeroeth element is exactly ROOT then it is
	 *            included if true and the zeroeth element is not ROOT it is
	 *            still included if false and ROOT it is not included if false
	 *            and not ROOT it is included
	 * @return
	 */
	public static final String stackToStringPath(Stack<Object> stack, boolean includeROOT) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < stack.size(); i++) {
			if (b.length() > 0) {
				b.append("/");
			}
			if (i == 0 && includeROOT == false && stack.elementAt(i).equals("ROOT")) {
				continue;
			}
			b.append("" + stack.elementAt(i));
		}
		return b.toString();
	}

	public static void recurse(Object parent, Stack<Object> pathStack, FakeConsumer<PathValue> consumer) {
		consumer.accept(new PathValue(pathStack, parent));
		if (JSONObject.class.equals(parent.getClass())) {
			JSONObject jo = (JSONObject) parent;
			for (String key : jo.keySet()) {
				pathStack.push(key);
				recurse(jo.get(key), pathStack, consumer);
				pathStack.pop();
			}
		} else if (JSONArray.class.equals(parent.getClass())) {
			JSONArray ja = (JSONArray) parent;
			for (int i = 0; i < ja.length(); i++) {
				pathStack.push(i);
				recurse(ja.get(i), pathStack, consumer);
				pathStack.pop();
			}
		}
	}

	public static boolean isJSONObject(Object o) {
		return JSONObject.class.equals(o.getClass());
	}

	public static boolean isJSONArray(Object o) {
		return JSONArray.class.equals(o.getClass());
	}

	/**
	 * 
	 * @param o
	 *            - JSONArray or JSONObject
	 * @throws ParseException
	 *             - if path is empty or doesn't exist in o
	 */
	public static void setObject(Object o, String path, Object value) throws ParseException {
		String[] a = path.trim().split("/");
		String parentPath = null;
		if (a.length > 0) {
			parentPath = flatten(a, "/", 0, a.length - 1);
		} else {
			throw new ParseException("path must not be empty");
		}
		Queue q = pathToQueue(parentPath);
		Object key = a[a.length - 1];
		Object parent = null;
		if (a.length == 1) {
			// a single value on the path is either a key to a JSONObject or an
			// index for a JSONArray
			parent = o;
		} else {
			parent = getObject(o, parentPath);
		}
		if (parent == null) {
			throw new ParseException("path does not exist in o");
		}
		try {
			if (JSONObject.class.equals(parent.getClass())) {
				((JSONObject) parent).put("" + key, value);
			} else {
				// must be JSONArray
				int index = Integer.parseInt("" + key);
				((JSONArray) parent).put(index, value);

			}
		} catch (Exception e) {
			throw new ParseException("path does not exist in o. " + e.getMessage());
		}
	}

	public static String[] pathToArray(String path) {
		String[] array = path.split("/");
		return array;
	}

	public static List<String> pathToList(String path) {
		return Arrays.asList(pathToArray(path));
	}

	public static Queue pathToQueue(String path) {
		return pathToQueue(path, 0, -1);
	}

	public static Queue pathToQueue(String path, int start) {
		return pathToQueue(path, start, -1);
	}

	public static Queue pathToQueue(String path, int start, int end) {
		String[] pathPieces = pathToArray(path);
		final int end2;
		if (end == -1) {
			end2 = Integer.MAX_VALUE;
		} else {
			end2 = end;
		}
		Queue<String> q = new LinkedList<String>();
		class Index {
			int index = 0;
		}

		for(int i=0;i<pathPieces.length;i++){
			if (i >= start && i < end2) {
				q.add(pathPieces[i]);
			}
		}
		return q;
	}

	public static Object getObject(Object parent, String path) {
		Queue q = pathToQueue(path);
		return getObject2(parent, q);
	}

	private static Object getObject2(Object parent, Queue q) {
		if (q.isEmpty()) {
			return parent;
		}
		Object key = q.remove();
		try {
			Object o = null;
			if (parent.getClass().equals(JSONObject.class)) {
				o = ((JSONObject) parent).get("" + key);
			} else if (parent.getClass().equals(JSONArray.class)) {
				o = ((JSONArray) parent).get(Integer.parseInt("" + key));
			}
			if (o != null) {
				return getObject2(o, q);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}
	public static <T> String flatten(T[] list, String delim, int start, int end) {
		StringBuffer b=new StringBuffer();
		if (list.length == 0) {
			return "";
		}

		for (int i = start; i < end; i++) {
			if (i > start) {
				b.append(delim);
			}
			b.append(list[i]);
		}
		return b.toString();
	}
}
