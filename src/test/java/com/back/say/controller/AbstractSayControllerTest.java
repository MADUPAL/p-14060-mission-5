package com.back.say.controller;

import com.back.say.repository.InMemorySayRepository;
import com.back.say.repository.SayRepository;
import com.back.say.service.SayService;
import com.back.standard.util.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractSayControllerTest {

    private ByteArrayOutputStream out;

    protected abstract SayRepository createRepository();

    @BeforeEach
    void setUp() {
        out = TestUtil.setOutToByteArray();
    }
    @AfterEach
    void tearDown() {
        TestUtil.clearSetOutToByteArray();
    }

    private String getOutput() {
        return out.toString(StandardCharsets.UTF_8).trim();
    }

    private SayController createControllerWithInput(Scanner input) {
        SayRepository repository = createRepository();
        SayService service = new SayService(repository);
        SayController controller = new SayController(service, input);

        return controller;
    }

    private String makeSampleData() {
        return IntStream
                .rangeClosed(1, 10)
                .mapToObj(id -> "등록\n명언 %d\n작자미상 %d".formatted(id, id))
                .collect(Collectors.joining("\n")) + "\n";
    }

    @Test
    @DisplayName("등록")
    public void 등록(){
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("명언 : ")
                .contains("작가 : ");
    }

    @Test
    @DisplayName("등록시 생성된 명언번호 노출")
    void t2() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("1번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("등록할 때 마다 생성되는 명언 번호가 증가")
    void t3() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                현재를 사랑하라.
                작자미상
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("1번 명언이 등록되었습니다.")
                .contains("2번 명언이 등록되었습니다.");
    }

    @Test
    @DisplayName("목록")
    void t4() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("번호 / 작가 / 명언")
                .contains("=====================")
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("삭제")
    void t5() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=2
                목록
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("2번 명언이 삭제되었습니다.")
                .contains("1 / 작자미상 / 현재를 사랑하라.")
                .doesNotContain("2 / 작자미상 / 과거에 집착하지 마라.");
    }

    @Test
    @DisplayName("삭제?id=1`를 2번 시도했을 때의 예외처리")
    void t6() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                삭제?id=1
                삭제?id=1
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("1번 명언이 삭제되었습니다.")
                .contains("1번 명언은 존재하지 않습니다.");
    }

    @Test
    @DisplayName("수정?id=1")
    void t7() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                수정?id=3
                수정?id=1
                현재를 사랑하세요.
                아리스토텔레스
                목록
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("3번 명언은 존재하지 않습니다.")
                .contains("명언(기존) : 현재를 사랑하라.")
                .contains("작가(기존) : 작자미상")
                .contains("1 / 아리스토텔레스 / 현재를 사랑하세요.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keywordType=content&keyword=과거")
    void t8() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=content&keyword=과거
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .doesNotContain("1 / 작자미상 / 현재를 사랑하라.");
    }

    @Test
    @DisplayName("목록?keywordType=author&keyword=작자")
    void t9() {
        //given
        String input = """
                등록
                현재를 사랑하라.
                작자미상
                등록
                과거에 집착하지 마라.
                작자미상
                목록?keywordType=author&keyword=작자
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("2 / 작자미상 / 과거에 집착하지 마라.")
                .contains("1 / 작자미상 / 현재를 사랑하라.");
    }
    @Test
    @DisplayName("목록?keyword=이순신")
    void t10() {
        //given
        String input = """
                등록
                나의 죽음을 적들에게 알리지 말라.
                이순신
                등록
                진정한 해전의 왕은 이순신 뿐이다.
                넬슨제독
                목록?keyword=이순신
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("1 / 이순신 / 나의 죽음을 적들에게 알리지 말라.")
                .contains("2 / 넬슨제독 / 진정한 해전의 왕은 이순신 뿐이다.");
    }

    @Test
    @DisplayName("목록 : 한 페이지에 최대 5개만 노출된다.")
    void t11() {
        //given
        String input = makeSampleData() + """
                목록
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("9 / 작자미상 9 / 명언 9")
                .contains("8 / 작자미상 8 / 명언 8")
                .contains("7 / 작자미상 7 / 명언 7")
                .contains("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .doesNotContain("4 / 작자미상 4 / 명언 4")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .doesNotContain("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1] / 2");
    }

    @Test
    @DisplayName("목록?page=2")
    void t12() {
        //given
        String input = makeSampleData() + """
                목록?page=2
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .doesNotContain("10 / 작자미상 10 / 명언 10")
                .doesNotContain("9 / 작자미상 9 / 명언 9")
                .doesNotContain("8 / 작자미상 8 / 명언 8")
                .doesNotContain("7 / 작자미상 7 / 명언 7")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .contains("5 / 작자미상 5 / 명언 5")
                .contains("4 / 작자미상 4 / 명언 4")
                .contains("3 / 작자미상 3 / 명언 3")
                .contains("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : 1 / [2]");
    }

    @Test
    @DisplayName("목록?keyword=명언")
    void t13() {
        //given
        String input = makeSampleData() + """
                목록
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .contains("10 / 작자미상 10 / 명언 10")
                .contains("9 / 작자미상 9 / 명언 9")
                .contains("8 / 작자미상 8 / 명언 8")
                .contains("7 / 작자미상 7 / 명언 7")
                .contains("6 / 작자미상 6 / 명언 6")
                .doesNotContain("5 / 작자미상 5 / 명언 5")
                .doesNotContain("4 / 작자미상 4 / 명언 4")
                .doesNotContain("3 / 작자미상 3 / 명언 3")
                .doesNotContain("2 / 작자미상 2 / 명언 2")
                .doesNotContain("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : [1] / 2");
    }

    @Test
    @DisplayName("목록?keyword=명언&page=2")
    void t14() {
        //given
        String input = makeSampleData() + """
                목록?keyword=명언&page=2
                종료
                """;
        Scanner sc = TestUtil.genScanner(input);
        SayController controller = createControllerWithInput(sc);

        //when
        controller.run();

        //then
        String output = getOutput();
        assertThat(output)
                .doesNotContain("10 / 작자미상 10 / 명언 10")
                .doesNotContain("9 / 작자미상 9 / 명언 9")
                .doesNotContain("8 / 작자미상 8 / 명언 8")
                .doesNotContain("7 / 작자미상 7 / 명언 7")
                .doesNotContain("6 / 작자미상 6 / 명언 6")
                .contains("5 / 작자미상 5 / 명언 5")
                .contains("4 / 작자미상 4 / 명언 4")
                .contains("3 / 작자미상 3 / 명언 3")
                .contains("2 / 작자미상 2 / 명언 2")
                .contains("1 / 작자미상 1 / 명언 1")
                .contains("페이지 : 1 / [2]");
    }
}
