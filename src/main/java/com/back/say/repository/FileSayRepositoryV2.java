package com.back.say.repository;

import com.back.say.domain.Say;
import com.back.say.dto.PageDto;
import com.back.say.dto.SayDto;
import com.back.say.exception.RepositoryException;
import com.back.say.utils.Pageable;
import com.back.say.utils.SaySearchCondition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.StandardOpenOption.*;

public class FileSayRepositoryV2 implements SayRepository{
    private final Path dirPath;
    private final Path idPath;
    private final Path dataPath;
    private static final String LAST_ID_FILE = "lastId.txt";
    private static final String DATA_FILE = "data.json";

    private List<Say> sayCache;
    private boolean loaded = false;

    public FileSayRepositoryV2() {
        dirPath = Path.of("db/wiseSaying/");
        idPath = dirPath.resolve(LAST_ID_FILE);
        dataPath = dirPath.resolve(DATA_FILE);
    }

    @Override
    public int create(SayDto dto) {
        loadDataIfNeeded();
        try {
            int id = nextId();
            Say say = new Say(id, dto.getAuthor(), dto.getContent());
            sayCache.add(say);
            writeAllJsonToFile(sayCache);
            return id;
        } catch (IOException e) {
            throw new RepositoryException("create 실패", e);
        }
    }

    @Override
    public int update(int id, SayDto dto) {
        loadDataIfNeeded();
        boolean found = false;
        for (int i = 0; i < sayCache.size(); i++) {
            if (sayCache.get(i).getId() == id) {
                sayCache.set(i, new Say(id, dto.getAuthor(), dto.getContent()));
                found = true;
                break;
            }
        }
        if (!found)
            return -1;
        try {
            writeAllJsonToFile(sayCache);
            return id;
        } catch (IOException e) {
            throw new RepositoryException("update 실패", e);
        }
    }

    @Override
    public int delete(int id) {
        loadDataIfNeeded();
        boolean removed = sayCache.removeIf(say -> say.getId() == id);
        if(!removed) return -1;
        try {
            writeAllJsonToFile(sayCache);
            return id;
        } catch (IOException e) {
            throw new RepositoryException("delete 실패", e);
        }
    }

    @Override
    public Optional<Say> findById(int id) {
        loadDataIfNeeded();
        return sayCache.stream().filter(say->say.getId() == id).findFirst();
    }

    @Override
    public List<Say> findAll() {
        loadDataIfNeeded();
        return sayCache.reversed().stream()
                .limit(5)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 10 단계
     */

    public void build() {
        try {
            writeAllJsonToFile(sayCache);
        } catch (IOException e) {
            throw new RepositoryException("build 실패", e);
        }
    }

    @Override
    public PageDto<Say> findPage(SaySearchCondition cond, Pageable pageable) {
        loadDataIfNeeded();
        Stream<Say> stream = sayCache.reversed().stream();

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

    private void loadDataIfNeeded() {
        if(loaded)
            return;
        try {
            ensureDir();
            String json = Files.readString(dataPath, UTF_8).trim();
            if (json.equals("[]") || json.isBlank()){
                sayCache = makeDummyData();
            } else {
                sayCache = parseJsonToSayList(json);
            }
            loaded = true;
        } catch (IOException e) {
            throw new RepositoryException("data.json 로드 실패", e);
        }
    }

    private void writeAllJsonToFile(List<Say> sayList) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < sayList.size(); i++) {
            if(i > 0)
                sb.append(",\n");
            sb.append(toJsonString(sayList.get(i)));
        }
        sb.append("\n]");
        Files.writeString(dataPath, sb.toString(), UTF_8, CREATE, TRUNCATE_EXISTING);

    }

    private void ensureDir() throws IOException {
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        if (Files.notExists(idPath)) {
            Files.writeString(idPath,"0", CREATE_NEW);
        }
        if (Files.notExists(dataPath)){
            Files.writeString(dataPath, "[]", UTF_8, CREATE_NEW);
        }
    }

    private Path fileForId(int id) {
        return dirPath.resolve(id + ".json");
    }

    private int nextId() throws IOException {
        int lastId = readLastId();
        int next = lastId + 1;
        writeLastId(next);
        return next;
    }

    private int readLastId() throws IOException {
        if (Files.notExists(idPath)) {
            return 0;
        }
        String txt = Files.readString(idPath, UTF_8).trim();
        if (txt.isEmpty()) return 0;

        return Integer.parseInt(txt);
    }

    private void writeLastId(int id) throws IOException {
        Files.writeString(
                idPath,
                Integer.toString(id),
                UTF_8,
                CREATE,              //lastId.txt 없으면 새로만들기 이거 빼면 파일 없을 때 예외 발생
                TRUNCATE_EXISTING    // 내용 삭제 후 새로 쓰기
        );
    }
    private void writeSingleJsonToFile(Path target, String json) throws IOException {
        Files.writeString(target, json, UTF_8, CREATE, TRUNCATE_EXISTING);
    }

    private String toJsonString(Say s) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t{\n");
        sb.append("\t\t\"id\": ").append(s.getId()).append(",\n");
        sb.append("\t\t\"content\": ").append("\"").append(s.getContent()).append("\",\n");
        sb.append("\t\t\"author\": ").append("\"").append(s.getAuthor()).append("\"\n");
        sb.append("\t}");
        return sb.toString();
    }
    private Say parseJsonToSay(String json) {
        if (json == null || json.isBlank())
            return null;
        int idIdx = json.indexOf("\"id\"");
        if (idIdx < 0)
            return null;
        int idColonIdx = json.indexOf(':', idIdx);
        int idCommaIdx = json.indexOf(',', idColonIdx);
        String idStr = json.substring(idColonIdx+1, idCommaIdx).trim();
        int id = Integer.parseInt(idStr);

        int contIdx = json.indexOf("\"content\"", idCommaIdx+1);
        if (contIdx < 0)
            return null;
        int contColonIdx = json.indexOf(':', contIdx);
        int contQuoteIdx = json.indexOf('"', contColonIdx);
        int i = contQuoteIdx + 1;

        StringBuilder content = new StringBuilder();

        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"')
                break;
            content.append(c);
        }

        int authIdx = json.indexOf("\"author\"", contIdx+1);
        if (authIdx < 0)
            return null;
        int authColonIdx = json.indexOf(':', authIdx);
        int authQuoteIdx = json.indexOf('"', authColonIdx);
        i = authQuoteIdx + 1;

        StringBuilder author = new StringBuilder();
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (c == '"')
                break;
            author.append(c);
        }

        return new Say(id, author.toString(), content.toString());
    }

    private List<Say> parseJsonToSayList(String json) {
        List<Say> sayList = new ArrayList<>();

        json = json.trim();
        if(json.startsWith("["))
            json = json.substring(1);
        if(json.endsWith("]"))
            json = json.substring(0,json.length()-1);

        String[] lines = json.split("},\\s*\\{");
        for (String line : lines) {
            line = line.replace("{", "").replace("}", "").trim();
//            System.out.println(parseJsonToSay(line));
            sayList.add(parseJsonToSay(line));
        }

        return sayList;
    }

    private List<Say> makeDummyData() throws IOException {
        ArrayList<Say> sayList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            sayList.add(new Say(i, "작자미상 "+i, "명언"+i));
        }
        writeAllJsonToFile(sayList);
        writeLastId(10);

        return sayList;
    }
}
