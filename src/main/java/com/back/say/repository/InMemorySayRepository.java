package com.back.say.repository;

import com.back.say.domain.Say;
import com.back.say.dto.PageDto;
import com.back.say.dto.SayDto;
import com.back.say.utils.Pageable;
import com.back.say.utils.SaySearchCondition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InMemorySayRepository implements SayRepository {

    private int id;
    private final HashMap<Integer, Say> sayMap;

    public InMemorySayRepository() {
        id = 0;
        sayMap = new HashMap<>();

        /*for (int i = 1; i <= 10; i++) {
            id++;
            sayMap.put(id, new Say(id, "작자미상 " + i, "명언" + i));
        }*/
    }

    @Override
    public int create(SayDto dto) {
        id++;
        sayMap.put(id, new Say(id, dto.getAuthor(), dto.getContent()));

        return id;
    }

    @Override
    public int update(int id, SayDto dto) {
        if (!sayMap.containsKey(id))
            return -1;
        sayMap.put(id, new Say(id, dto.getAuthor(), dto.getContent()));
        return id;
    }

    @Override
    public int delete(int id) {
        if (!sayMap.containsKey(id))
            return -1;
        sayMap.remove(id);
        return id;
    }

    @Override
    public Optional<Say> findById(int id) {
        Say say = sayMap.get(id);
        if(say == null)
            return Optional.empty();
        return Optional.of(say);
    }

    @Override
    public List<Say> findAll() {
        return sortedStreamDescById()
//                .limit(5)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void build() {}

    @Override
    public PageDto<Say> findPage(SaySearchCondition cond, Pageable pageable) {
        Stream<Say> stream = sortedStreamDescById();

        if (cond.hasAuthorCondition() && cond.hasContentCondition()) {
            String k = cond.getAuthorContains();
            stream = stream.filter(say->say.getAuthor().contains(k) || say.getContent().contains(k));
        } else if (cond.hasAuthorCondition()) {
            String k = cond.getAuthorContains();
            stream = stream.filter(say->say.getAuthor().contains(k));
        } else if (cond.hasContentCondition()) {
            String k = cond.getContentContains();
            stream = stream.filter(say->say.getContent().contains(k));
        }

        List<Say> filtered = stream.toList();
        int totalCount = filtered.size();

        int from = Math.min(pageable.getOffset(), totalCount);
        int to = Math.min(from + pageable.getPageSize(), totalCount);

        List<Say> pageContent = filtered.subList(from, to);

        return new PageDto<>(pageContent, pageable.getPageNo(), pageable.getPageSize(), totalCount);
    }

    private Stream<Say> sortedStreamDescById() {
        return sayMap.values().stream()
                .sorted(Comparator.comparingInt(Say::getId).reversed());
    }
}
