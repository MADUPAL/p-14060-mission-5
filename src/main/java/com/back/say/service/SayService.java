package com.back.say.service;

import com.back.say.domain.Say;
import com.back.say.dto.PageDto;
import com.back.say.dto.ResponseSayDto;
import com.back.say.dto.SayDto;
import com.back.say.exception.SayNotFoundException;
import com.back.say.repository.SayRepository;
import com.back.say.utils.Pageable;
import com.back.say.utils.SaySearchCondition;

import java.util.List;
import java.util.Optional;

public class SayService {
    private final SayRepository sayRepository;

    public SayService(SayRepository sayRepository) {
        this.sayRepository = sayRepository;
    }

    public int create(SayDto dto) {
        if (dto.getAuthor() == null || dto.getAuthor().isBlank())
            throw new IllegalArgumentException("작가는 비어있을 수 없습니다.");
        if (dto.getContent() == null || dto.getContent().isBlank())
            throw new IllegalArgumentException("명언은 비어있을 수 없습니다.");

        return sayRepository.create(dto);
    }

    public ResponseSayDto findById(int id) {
        Optional<Say> result = sayRepository.findById(id);
        if (result.isEmpty())
            throw new SayNotFoundException(id);
        Say say = result.get();
        return new ResponseSayDto(say.getId(), say.getAuthor(), say.getContent());
    }

    public List<ResponseSayDto> findAll() {
        return sayRepository.findAll().stream()
                .map(say->new ResponseSayDto(say.getId(), say.getAuthor(), say.getContent()))
                .toList();
    }

    public int delete(int id) {
        int deletedId = sayRepository.delete(id);
        if (deletedId == -1)
            throw new SayNotFoundException(id);
        return deletedId;
    }

    public int update(int id, SayDto dto) {
        int updatedId = sayRepository.update(id, dto);
        if (updatedId == -1)
            throw new SayNotFoundException(id);
        return updatedId;
    }

    public void build() {
        sayRepository.build();
    }

/*    public List<ResponseSayDto> findByAuthorContains(String keyword, Pageable pageable) {
        List<Say> byAuthorContains = sayRepository.findByAuthorContains(keyword, pageable);
        return toDtoList(byAuthorContains);

    }
    public List<ResponseSayDto> findByContentContains(String keyword, Pageable pageable) {
        List<Say> byContentContains = sayRepository.findByContentContains(keyword, pageable);
        return toDtoList(byContentContains);
    }
    public List<ResponseSayDto> findByAuthorContainsOrContentContains(String keyword, Pageable pageable) {
        List<Say> byAuthorOrContentContains = sayRepository.findByAuthorContainsOrContentContains(keyword, pageable);
        return toDtoList(byAuthorOrContentContains);
    }*/

    public PageDto<ResponseSayDto> getPage(String keywordType, String keyword, Pageable pageable) {
        SaySearchCondition cond;

        if (keyword == null || keyword.isBlank()) {
            cond = new SaySearchCondition(null, null);
        } else if ("author".equals(keywordType)) {
            cond = new SaySearchCondition(keyword, null);
        } else if ("content".equals(keywordType)) {
            cond = new SaySearchCondition(null, keyword);
        } else { // all
            cond = new SaySearchCondition(keyword, keyword);
        }

        PageDto<Say> page = sayRepository.findPage(cond, pageable);

        List<ResponseSayDto> dtoList = page.getContent().stream()
                .map(say -> new ResponseSayDto(say.getId(), say.getAuthor(), say.getContent()))
                .toList();

        return new PageDto<>(dtoList,
                page.getPageNo(),
                page.getPageSize(),
                page.getTotalCount());
    }
    private List<ResponseSayDto> findRecentTop5() {
        return findAll();
    }

    private List<ResponseSayDto> toDtoList(List<Say> sayList) {
        return sayList.stream()
                .map(say -> new ResponseSayDto(say.getId(), say.getAuthor(), say.getContent()))
                .toList();
    }
}
