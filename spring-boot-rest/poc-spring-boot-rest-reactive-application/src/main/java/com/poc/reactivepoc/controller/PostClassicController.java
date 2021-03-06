/*
 * Copyright 2015-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.poc.reactivepoc.controller;

import java.net.URI;

import com.poc.reactivepoc.entity.ReactivePost;
import com.poc.reactivepoc.service.PostService;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostClassicController {

	private final PostService postService;

	private final MediaType mediaType = MediaType.APPLICATION_JSON;

	@GetMapping
	public Publisher<ReactivePost> all() {
		return this.postService.findAllPosts();
	}

	@GetMapping("/{id}")
	public Publisher<ReactivePost> get(@PathVariable("id") Integer id) {
		return this.postService.findPostById(id);
	}

	@PostMapping
	public Publisher<ResponseEntity<ReactivePost>> create(@RequestBody ReactivePost reactivePost) {
		return this.postService.savePost(reactivePost).map(persistedPost -> ResponseEntity
				.created(URI.create("/posts/" + persistedPost.getId())).contentType(this.mediaType).build());
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<ReactivePost>> update(@PathVariable("id") Integer id,
			@RequestBody ReactivePost reactivePost) {
		return Mono.just(reactivePost).flatMap(post -> this.postService.update(id, post))
				.map(p -> ResponseEntity.ok().contentType(this.mediaType).build());
	}

	@DeleteMapping("/{id}")
	public Publisher<ReactivePost> delete(@PathVariable("id") Integer id) {
		return this.postService.deletePostById(id);
	}

}
