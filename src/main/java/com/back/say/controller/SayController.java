package com.back.say.controller;

import com.back.say.dto.PageDto;
import com.back.say.dto.ResponseSayDto;
import com.back.say.dto.SayDto;
import com.back.say.exception.SayNotFoundException;
import com.back.say.service.SayService;
import com.back.say.utils.Pageable;
import com.back.say.utils.Rq;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SayController {
    private final SayService sayService;
    private final Scanner sc;

    public SayController(SayService sayService, Scanner sc) {
        this.sayService = sayService;
        this.sc = sc;
    }

    public void run() {
        System.out.println("== 명언 앱 ==");
        while (true) {
            System.out.print("명령) ");
            String cmd = sc.nextLine().trim();
            if (cmd.equals("종료")){
                System.out.println("== 앱 종료 ==");
                break;
            }

            Rq rq = new Rq(cmd);
            handle(rq);
        }
    }

    private void handle(Rq rq) {
        switch (rq.getActionName()) {
            case "등록" -> handleCreate();
            case "목록" -> handleList(rq);
            case "삭제" -> {
                int id = rq.getParamAsInt("id", -1);
                if (id == -1) {
                    System.out.println("id를 숫자로 입력해주세요");
                    return;
                }
                handleDelete(id);
            }
            case "수정" -> {

                int id = rq.getParamAsInt("id", -1);
                if (id == -1) {
                    System.out.println("id를 숫자로 입력해주세요");
                    return;
                }
                handleUpdate(id);
            }
            case "빌드" -> handleBuild();
            case "도움" -> handleHelp();
        }
    }

    private void handleBuild() {
        sayService.build();
    }

    private void handleHelp() {
        System.out.println("""
                - 명령어 목록 -
                1. 등록
                2. 목록
                3. 삭제?id={번호}
                4. 수정?id={번호}
                """);
    }

    private void handleCreate() {
        try {
            System.out.print("명언 : ");
            String content = sc.nextLine().trim();

            System.out.print("작가 : ");
            String author = sc.nextLine().trim();

            int savedId = sayService.create(new SayDto(author, content));
            System.out.println(savedId + "번 명언이 등록되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void handleList(Rq rq) {
        try {
            String keywordType = rq.getParam("keywordType", "all");
            String keyword = rq.getParam("keyword", "");
            int page = rq.getParamAsInt("page", 1);
            Pageable pageable = new Pageable(page, 5);

            PageDto<ResponseSayDto> result = sayService.getPage(keywordType, keyword, pageable);

            System.out.println("번호 / 작가 / 명언");
            System.out.println("=====================");
            printDto(result.getContent());
            printPage(result);

        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
        }
    }

    private static void printDto(List<ResponseSayDto> dtoList) {
        for (ResponseSayDto sayData : dtoList) {
            System.out.println(sayData);
        }
    }

    private void handleDelete(int id) {
        try {
            sayService.findById(id);
            int deletedId = sayService.delete(id);
            System.out.println(deletedId + "번 명언이 삭제되었습니다.");
        } catch (SayNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void handleUpdate(int id) {
        try {
            ResponseSayDto result = sayService.findById(id);
            System.out.println("명언(기존) : " + result.getContent());
            System.out.print("명언 : ");
            String updateContent = sc.nextLine();

            System.out.println("작가(기존) : " + result.getAuthor());
            System.out.print("작가 : ");
            String updateAuthor = sc.nextLine();

            int updatedId = sayService.update(id, new SayDto(updateAuthor, updateContent));
            System.out.println(updatedId + "번 명언이 수정되었습니다.");
        } catch (SayNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("오류가 발생했습니다: " + e.getMessage());
        }

    }

    private void printPage(PageDto<?> result) {
        int total = result.getTotalPages();
        int curr = result.getPageNo();
        if (total == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("페이지 : ");

        for (int i = 1; i <= total; i++) {
            if (i == curr) sb.append("[").append(i).append("]");
            else sb.append(i);

            if (i < total) sb.append(" / ");
        }

        System.out.println(sb);
    }
}
