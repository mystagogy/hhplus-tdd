package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PointServiceTest {

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    @Autowired
    private PointService pointService;

    @Test
    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성
     * 1. 사용자 정보 저장
     * 2. 해당 아이디로 포인트 조회
     * 3. 조회된 포인트 일치 여부 확인
     */
    void 사용자_포인트_조회() {
        //given
        Long id = 1L;
        userPointTable.insertOrUpdate(id, 1000L);

        //when
        UserPoint userPoint = pointService.getUserPoint(id);


        //then
        assertEquals(id, userPoint.id());
        assertEquals(1000L, userPoint.point());
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성
     * 1. 존재하지 않는 사용자 아이디로 포인트 조회
     * 2. 존지하지 않는 경우 포인트가 0으로 생성
     * 3. 생성된 사용자 포인트 값 확인
     */
    @Test
    void 존재하지_않는_사용자_포인트_조회() {
        //given
        Long id = 2L;

        //when
        UserPoint userPoint = pointService.getUserPoint(id);

        //then
        assertEquals(0L, userPoint.point());
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회
     * 1. 사용자 포인트 충전
     * 2. 사용자 거래내역 생성
     * 2. 사용자 포인트 충전 및 거래내역 조회
     */
    @Test
    void 사용자_포인트내역_조회() {
        //given
        long id = 1L;
        pointHistoryTable.insert(1L, 1000L, TransactionType.CHARGE, 123456789L);
        pointHistoryTable.insert(1L, 4500L, TransactionType.CHARGE, 123456789L);
        pointHistoryTable.insert(1L, 3000L, TransactionType.USE, 123456789L);

        //when
        List<PointHistory> historyList = pointService.getPointHistory(id);

        //then
        assertEquals(id, historyList.get(0).userId()); //
        assertEquals(1000L, historyList.get(0).amount());
        assertEquals(TransactionType.CHARGE, historyList.get(0).type());
        assertEquals(TransactionType.USE, historyList.get(2).type());
        assertEquals(3000L, historyList.get(2).amount());
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회
     * 1. 포인트 사용 이력 없는 사용자 아이디 조회
     * 2. 조회된 리스트 확인
     */
    @Test
    void 거래내역_없는_사용자_포인트_내역_조회(){
        //given
        Long id = 123L;

        //when
        List<PointHistory> historyList = pointService.getPointHistory(id);

        //then
        assertEquals(historyList.size(), 0);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전
     * 1. 사용자 아이디로 포인트 충전
     * 2. 사용자 포인트 이력 조회
     * 3. 마지막 이력 확인
     */
    @Test
    void 사용자_포인트_충전() {
        //given
        long id = 1L;

        //when
        UserPoint rtn = pointService.chargePoint(id, 2000L);
        List<PointHistory> list = pointService.getPointHistory(id);
        int num = list.size()-1;

        //then
        assertEquals(2000L, rtn.point());
        assertEquals(2000L, list.get(num).amount());
        assertEquals(TransactionType.CHARGE, list.get(num).type());

    }

    @Test
    void 동시_포인트_충전() throws InterruptedException {
        //given
        long id = 100L;
        UserPoint userPoint = pointService.getUserPoint(id);

        long amount = 2000L;
        int threadCnt = 5; //동시에 실행한 스레드 수
        ExecutorService executor = Executors.newFixedThreadPool(threadCnt); //여러 스레드 동시 실행
        CountDownLatch latch = new CountDownLatch(threadCnt); //동기화 도구

        for (int i = 0; i < threadCnt; i++) { //5개의 스레드가 동시에 충전
            executor.submit(() -> {
                pointService.chargePoint(id, amount);
                latch.countDown(); //작업 완료
            });
        }

        latch.await(); //스레드 종료 대기
        executor.shutdown(); //스레드 풀 종료

        UserPoint rtnPoint = pointService.getUserPoint(id);

        // 예상: 2000*5 = 10000
        assertEquals(10000L, rtnPoint.point());

    }

    /**
     * TODO - 특정 유저의 포인트를 사용
     */
    @Test
    void 사용자_포인트_사용() {
        //given
        long id = 1L;
        userPointTable.insertOrUpdate(1L, 10000L);

        //when
        UserPoint userPoint = pointService.usePoint(id, 2500L);

        //then
        assertEquals(7500L, userPoint.point());
    }

    @Test
    void 사용자_포인트_부족() {
        //given
        long id = 1L;

        //when & then
        assertThrows(IllegalArgumentException.class, () -> {
            pointService.usePoint(id, 10000L);
        });

    }
}