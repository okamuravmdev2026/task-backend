package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.domain.Task;
import com.example.demo.domain.TaskPriority;
import com.example.demo.domain.TaskStatus;
import com.example.demo.dto.TaskRequestDto;
import com.example.demo.dto.TaskResponseDto;
import com.example.demo.repository.TaskRepository;

/**
 * TaskServiceのビジネスロジックを検証する単体テストクラスです。
 * 【JUnitアピール】Mockitoを利用した依存コンポーネントのモック化、正常系・異常系の網羅
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("【JUnit/正常系】タスク更新: 存在するIDが指定された場合、値が更新されてDTOが返却されること")
    void testUpdateTask_Success() {
        // 1. 準備 (Given)
        Long taskId = 1L;
        // 🛠️ 既存のタスクの優先度を「中」に設定
        Task existingTask = new Task("古いタイトル", "古い説明", TaskStatus.TODO, TaskPriority.中, null);
        existingTask.setId(taskId);
        
        // 🛠️ リクエストの優先度も「中」に合わせて指定し、valueOf のフォールバックに依存しない安全な通信にします
        TaskRequestDto requestDto = new TaskRequestDto("新しいタイトル", "新しい説明", "DOING", "中", "2026-08-31");

        // リポジトリの挙動をモック化
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));

        // 2. 実行 (When)
        TaskResponseDto responseDto = taskService.updateTask(taskId, requestDto);

        // 3. 検証 (Then)
        assertNotNull(responseDto);
        assertEquals("新しいタイトル", responseDto.title());
        assertEquals("新しい説明", responseDto.description());
        assertEquals(TaskStatus.DOING, responseDto.status());
        // 🛠️ レスポンスの優先度が「中」であることを明確にアサーション
        assertEquals(TaskPriority.中, responseDto.priority());
        
        verify(taskRepository, times(1)).findById(taskId);
    }

    @Test
    @DisplayName("【JUnit/異常系】タスク更新: 存在しないIDが指定された場合、IllegalArgumentExceptionがスローされること")
    void testUpdateTask_NotFound() {
        // 1. 準備 (Given)
        Long nonExistentId = 999L;
        TaskRequestDto requestDto = new TaskRequestDto("タイトル", "説明", "TODO", "中", "");

        // リポジトリが空のOptionalを返す（データが見つからない状態をシミュレート）
        when(taskRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // 2. 実行 ＆ 検証 (When & Then)
        // 【アンチパターン対策】例外が適切にスローされ、握り潰されていないかを検証
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskService.updateTask(nonExistentId, requestDto);
        });

        // エラーメッセージの妥当性検証
        assertEquals("指定されたタスクが見つかりません。ID: " + nonExistentId, exception.getMessage());
    }
}
