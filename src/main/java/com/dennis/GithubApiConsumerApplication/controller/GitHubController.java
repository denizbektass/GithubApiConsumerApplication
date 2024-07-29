package com.dennis.GithubApiConsumerApplication.controller;

import com.dennis.GithubApiConsumerApplication.entity.Branch;
import com.dennis.GithubApiConsumerApplication.entity.Repository;
import com.dennis.exception.Error;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubController {

    private final RestTemplate restTemplate;

    private static final String GITHUB_API_URL = "https://api.github.com";

    @GetMapping("/repos")
    public ResponseEntity<?> getUserRepositories(@RequestParam String username, @RequestHeader("Accept") String accept) {

        try {
            String userReposUrl = GITHUB_API_URL + "/users/" + username + "/repos";
            Repository[] repositories = restTemplate.getForObject(userReposUrl, Repository[].class);

            if (repositories == null || repositories.length == 0) {
                return new ResponseEntity<>(new Error(404, "User not found"), HttpStatus.NOT_FOUND);
            }

            List<Repository> filteredRepos = new ArrayList<>();
            for (Repository repo : repositories) {
                if (!repo.isFork()) {
                    String branchesUrl = GITHUB_API_URL + "/repos/" + username + "/" + repo.getName() + "/branches";
                    Branch[] branches = restTemplate.getForObject(branchesUrl, Branch[].class);
                    if (branches != null) {
                        repo.setBranches(List.of(branches));
                        filteredRepos.add(repo);
                    }
                }
            }
            return ResponseEntity.ok(filteredRepos);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(new Error(e.getStatusCode().value(), "User not found"), HttpStatus.NOT_FOUND);
        }
    }
}
