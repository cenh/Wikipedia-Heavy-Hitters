package it.alexincerti.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.alexincerti.service.CategoryPath;
import it.alexincerti.service.ShortestPathCalculator;

@RestController
public class HelloController {

	@Autowired
	private ShortestPathCalculator shortestPathCalculator;

	public ShortestPathCalculator getShortestPathCalculator() {
		return shortestPathCalculator;
	}

	public Map<String, CategoryPath> getCategoryMapping(String startCategory, List<String> endNodes,
			int maxPathLength) {
		List<CategoryPath> paths = getShortestPathCalculator().getShortestPathList(startCategory, endNodes,
				maxPathLength);
		Map<String, CategoryPath> categoryCountMap = paths.stream().collect(Collectors.groupingBy(
				CategoryPath::getEndCategory, //
				Collectors.collectingAndThen(Collectors.reducing(
						(CategoryPath d1, CategoryPath d2) -> d1.getPath().size() > d2.getPath().size() ? d1 : d2),
						Optional::get)));
//		String macroCategory = categoryCountMap.keySet().stream()
//				.min((k1, k2) -> categoryCountMap.get(k1).compareTo(categoryCountMap.get(k2))).orElse(null);
		return categoryCountMap;
	}

	public String getMacroCategoryMapping(List<String> startCategories, List<String> endCategories, int maxPathLength) {
		Map<String, Long> mappingCount = new HashMap<>();
		startCategories.forEach(startCategory -> {
			Map<String, CategoryPath> paths = getCategoryMapping(startCategory, endCategories, 20);
			paths.forEach((k, v) -> {
				mappingCount.put(k, mappingCount.getOrDefault(k, Long.MAX_VALUE) + v.getLength());
			});
		});

		String category = mappingCount.keySet().stream()
				.min((k1, k2) -> mappingCount.get(k1).compareTo(mappingCount.get(k2))).orElse("");
//		String macroCategory = categoryCountMap.keySet().stream()
//				.min((k1, k2) -> categoryCountMap.get(k1).compareTo(categoryCountMap.get(k2))).orElse(null);
		return category;
	}

	@GetMapping("/mapCategory")
	public String mappingEntrypoint(@RequestParam("startCategories") String startCategories,
			@RequestParam("endCategories") String endCategories) {
		List<String> startNodes = Arrays.asList(startCategories.split("::"));
		List<String> endNodes = Arrays.asList(endCategories.split("::"));

		return getMacroCategoryMapping(startNodes, endNodes, 20);
	}

}