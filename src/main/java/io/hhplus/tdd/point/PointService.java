package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;


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

        if(amount <= 0) throw new IllegalArgumentException("충전금액은 0원을 초과해야 합니다.");
        //사용자 조회
        UserPoint userPoint = userPointTable.selectById(id);

        //포인트 추가 및 이력 업데이트
        long point = userPoint.point() + amount;
        UserPoint rtnUserPoint = userPointTable.insertOrUpdate(id, point);
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, 123456789L);

        return rtnUserPoint;
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
