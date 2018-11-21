package com.github.jacopofar.wikipediacategorygraph;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

class TestParsePageFile {

	@Test
	@Ignore
	void test() throws FileNotFoundException, IOException {
		String testFile = "C:\\Users\\Alex\\Desktop\\test.txt";
		String result = "C:\\Users\\Alex\\Desktop\\testresult.txt";

		Set<String> expectedResults = loadResults(result);

		try (BufferedReader br = new BufferedReader(new FileReader(testFile))) {
			br.lines().forEach(line -> {
				List<String> pages = FileParser.getPages(line);
				pages.forEach(page -> {
					String pageName = FileParser.getPageName(page);
					System.out.println(pageName);
					Assert.assertTrue(expectedResults.contains(pageName));
					expectedResults.remove(pageName);
				});
			});
		}

		Assert.assertTrue(expectedResults.isEmpty());
	}

	@Test
	void test2() throws FileNotFoundException, IOException {
		ConcurrentHashMap<Integer, String> loadResults = buildPageDictionary(
				"C:\\Users\\Alex\\Code\\Wikipedia-Confidence-Indicator\\WikipediaCategoryGraph\\wikipedia-category-graph-master\\pageid_categoryname.txt");
		System.out.println(loadResults.get(691023));
	}

	private Set<String> loadResults(String resultFile) throws FileNotFoundException, IOException {
		Set<String> retval = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(resultFile))) {
			br.lines().forEach(retval::add);
		}
		return retval;
	}

	private static void parseWithCommas(String pageLinkFile) throws FileNotFoundException, IOException {
		try (BufferedReader br = new BufferedReader(new FileReader(pageLinkFile))) {
			br.lines().forEach(line -> {
				// while((line=br.readLine())!=null){
				// pick the lines containing inserts, not comments or DDL
				if (!line.startsWith("INSERT INTO ") || line.length() < 2)
					return;

				for (String s : line.split(Pattern.quote("),("))) {
					System.out.println(s);
				}
			});
		}

	}

	private static ConcurrentHashMap<Integer, String> buildPageDictionary(String file) throws IOException {
		ConcurrentHashMap<Integer, String> dictionary = new ConcurrentHashMap<Integer, String>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] ls = line.split(":");
			Integer id = Integer.parseInt(ls[0].replace(" ", ""));
			String name = ls[1].replace(" ", "");
			dictionary.put(id, name);
		}
		return dictionary;
	}

	private static void createIDCategoryNameFromPageLink(String pageLinkFile)
			throws FileNotFoundException, IOException {
		Pattern pattern = Pattern.compile("\\(([^()]*)\\)");
		// read the file line by line
		try (BufferedReader br = new BufferedReader(new FileReader(pageLinkFile))) {
			br.lines().forEach(line -> {
				// while((line=br.readLine())!=null){
				// pick the lines containing inserts, not comments or DDL
				if (!line.startsWith("INSERT INTO ") || line.length() < 2)
					return;

				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String page = matcher.group();
					page = page.replace("(", "");
					page = page.replace(")", "");
					String name = page.split(",")[2];
					int ID = Integer.parseInt(page.split(",")[0].replaceAll(",'.+", ""));
//								if (done.incrementAndGet() % 100000 == 0)
//									System.out.println(" - loaded " + done.get() + " categories");
				}
			});
//			System.out.println("Loaded " + done.get() + " categories in "
//					+ (System.currentTimeMillis() - lastTime) / 1000 + " seconds");
		}
	}

}
