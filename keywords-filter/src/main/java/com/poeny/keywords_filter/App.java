package com.poeny.keywords_filter;

import java.util.Set;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		ConnectionManager.getInstance().init();
		Keywords keywords = Keywords.getInstance();
		Updater updater = Updater.getInstance();
		long e1, e2,ex1,ex2;
		try {
			
			ex1 = e1 = System.currentTimeMillis();
//			Set<String> s = keywords.getAll();
//			Set<String> t = keywords.getRemoteTypeNotEqualTo(10);
//			Set<String> set = Sets.union(s, t);
//			System.out.println("初始单词个数 ： " + s.size());
//			e2 = System.currentTimeMillis();
//			System.out.println("耗时 ： " + (e2 - e1) + "ms");
//
//			e1 = System.currentTimeMillis();
//			Filter.getFilter().filter(set);
//			System.out.println(s);
//			System.out.println(s.size());
//			e2 = System.currentTimeMillis();
//			System.out.println(e2 - e1 + "ms");
//			
//			e1 = System.currentTimeMillis();
//			
//			updater.updateToRemote(set);

			updater.testInsert();
			ex2 = e2 = System.currentTimeMillis();
//			System.out.println(e2 - e1 + "ms");
			
			System.out.println("总耗时：" + (ex2 - ex1) + "ms");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}
}
