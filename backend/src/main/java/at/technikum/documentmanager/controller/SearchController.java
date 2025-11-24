package at.technikum.documentmanager.controller;

import at.technikum.documentmanager.search.SearchResult;
import at.technikum.documentmanager.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public List<SearchResult> search(@RequestParam("q") String query) {
        return searchService.search(query);
    }
}
