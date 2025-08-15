package io.hhplus.tdd.point;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PointController.class)
class PointControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PointService pointService;


    @Test
    void 사용자_포인트_조회() throws Exception {
        //given
        long id = 1L;
        UserPoint userPoint = new UserPoint(id, 1000L, 123L);
        when(pointService.getUserPoint(id)).thenReturn(userPoint);
        //when

        //then
        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.point").value(1000));

    }

    @Test
    void 사용자_포인트_거래내역_조회() throws Exception{
        //given
        long id = 1L;
        List<PointHistory> historyList = new ArrayList<>();
        PointHistory pointHistory1 = new PointHistory(1, 1L, 5000L, TransactionType.CHARGE, 123L);
        PointHistory pointHistory2 = new PointHistory(2, 1L, 3000L, TransactionType.USE, 123L);
        historyList.add(pointHistory1);
        historyList.add(pointHistory2);

        given(pointService.getPointHistory(1L)).willReturn(historyList);

        //when

        //then
        mockMvc.perform(get("/point/{id}/histories", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].amount").value(5000))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].amount").value(3000))
                .andExpect(jsonPath("$[1].type").value("USE"));
    }

    @Test
    void 사용자_포인트_충전() throws Exception{
        //given
        long id = 1L;
        long amount = 2000L;
        UserPoint userPoint = new UserPoint(id, amount, 123L);
        given(pointService.chargePoint(id, amount)).willReturn(userPoint);

        //when

        //then
        mockMvc.perform(
                        patch("/point/{id}/charge", id)
                                .content("2000")
                                .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(amount));

    }

    @Test
    void 사용자_포인트_사용() throws Exception{
        //given
        long id = 1L;
        long amount = 500L;
        UserPoint userPoint = new UserPoint(id, 1500L, 234L);
        given(pointService.usePoint(id, amount)).willReturn(userPoint);

        //when

        //then
        mockMvc.perform(
                        patch("/point/{id}/use", id)
                                .content("500") // JSON 숫자만
                                .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(1500L));
    }



}