package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    private final Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>(); //사용자별 Lock 관리 Map

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    public UserPoint getUserPoint(Long id) {
        return userPointTable.selectById(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    public List<PointHistory> getPointHistory(Long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }


    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    public UserPoint chargePoint(Long id, Long amount) {

        locks.putIfAbsent(id, new ReentrantLock()); //Lock 초기화
        ReentrantLock lock = locks.get(id);

        lock.lock(); //다른 스레드 접근 불가

        try {
            if(amount <= 0) throw new IllegalArgumentException("충전금액은 0원을 초과해야 합니다.");
            //사용자 조회
            UserPoint userPoint = userPointTable.selectById(id);

            //포인트 추가 및 이력 업데이트
            long point = userPoint.point() + amount;
            UserPoint rtnUserPoint = userPointTable.insertOrUpdate(id, point);
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, 123456789L);

            return rtnUserPoint;
        } finally {
            lock.unlock(); //Lock 해제
        }
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    public UserPoint usePoint(Long id, Long amount){

        if(amount <= 0) throw new IllegalArgumentException("사용금액은 0원을 초과해야 합니다.");

        //사용자 조회
        UserPoint userPoint = userPointTable.selectById(id);

        if(userPoint.point() < amount) throw new IllegalArgumentException("사용할 수 있는 포인트가 부족합니다.");


        //포인트 사용 및 이력 업데이트
        long point = userPoint.point() - amount;
        UserPoint rtnUserPoint = userPointTable.insertOrUpdate(id, point);
        pointHistoryTable.insert(id, amount, TransactionType.USE, 123456789L);

        return rtnUserPoint;
    }

}
