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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class FileSayRepositoryV1 implements SayRepository{
    private final Path dirPath;
    private final Path idPath;
    private static final String LAST_ID_FILE = "lastId.txt";

    public FileSayRepositoryV1() {
        dirPath = Path.of("db/wiseSaying/");
        idPath = dirPath.resolve(LAST_ID_FILE);
    }

    @Override
    public int create(SayDto dto) {
        try {
            ensureDir();
            int id = nextId();
            Say say = new Say(id, dto.getAuthor(), dto.getContent());
            Path target = fileForId(id);
            writeJsonToFile(target, toJsonString(say));
            return id;
        } catch (IOException e) {
            throw new RepositoryException("create 실패", e);
        }
    }

    @Override
    public int update(int id, SayDto dto) {
        try {
            ensureDir();
            Path target = fileForId(id);
            if (Files.notExists(target))
                return -1;

            Say newSay = new Say(id, dto.getAuthor(), dto.getContent());
            writeJsonToFile(target, toJsonString(newSay));
            return id;
        } catch (IOException e) {
            throw new RepositoryException("update 실패", e);
        }
    }

    @Override
    public int delete(int id) {
        try {
            ensureDir();
            Path target = fileForId(id);
            if (Files.notExists(target))
                return -1;

            Files.delete(target);
            return id;
        } catch (IOException e) {
            throw new RepositoryException("delete 실패", e);
        }
    }

    @Override
    public Optional<Say> findById(int id) {
        try {
            ensureDir();
            Path target = fileForId(id);
            if (Files.notExists(target))
                return Optional.empty();

            String sayStr = Files.readString(target, StandardCharsets.UTF_8);
            Say say = parseJsonToSay(sayStr);
            return Optional.of(say);
        } catch (IOException e) {
            throw new RepositoryException("findById 실패", e);
        }
    }

    @Override
    public List<Say> findAll() {
        // db/wiseSaying하위 파일 다 불러오기 Files.list(dirPath) -> stream이므로 close() 필수
        try {
            ensureDir();

            try(Stream<Path> fileList = Files.list(dirPath)) {

                return fileList
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .map(path -> {
                            try {
                                String json = Files.readString(path, StandardCharsets.UTF_8);
                                return parseJsonToSay(json);
                            } catch (IOException e2) {
                                throw new RepositoryException("json 파일 읽기 실패 :" + path, e2);
                            }
                        })
//                        .filter(say -> say != null)
                        .toList();

            }
        } catch (IOException e1) {
            throw new RepositoryException("findAll 실패", e1);
        }
    }

    /**
     * 10 단계
     */
    @Override
    public void build() {

    }

    /**
     *
     * 미구현
     */

    @Override
    public PageDto<Say> findPage(SaySearchCondition cond, Pageable pageable) {
        return null;
    }

    private void ensureDir() throws IOException {
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        if (Files.notExists(idPath)) {
            Files.writeString(idPath,"0", StandardOpenOption.CREATE_NEW);
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
        String txt = Files.readString(idPath, StandardCharsets.UTF_8).trim();
        if (txt.isEmpty()) return 0;

        return Integer.parseInt(txt);
    }

    private void writeLastId(int id) throws IOException {
        Files.writeString(
                idPath,
                Integer.toString(id),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,              //lastId.txt 없으면 새로만들기 이거 빼면 파일 없을 때 예외 발생
                StandardOpenOption.TRUNCATE_EXISTING    // 내용 삭제 후 새로 쓰기
        );
    }
    private void writeJsonToFile(Path target, String json) throws IOException {
        Files.writeString(target, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private String toJsonString(Say s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\t\"id\": ").append(s.getId()).append(",\n");
        sb.append("\t\"content\": ").append("\"").append(s.getContent()).append("\",\n");
        sb.append("\t\"author\": ").append("\"").append(s.getAuthor()).append("\"\n");
        sb.append("}\n");
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
}
