package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Task;
import com.example.demo.domain.TaskStatus;
import com.example.demo.dto.TaskRequestDto;
import com.example.demo.dto.TaskResponseDto;
import com.example.demo.repository.TaskRepository;

/**
 * タスク管理のビジネスロジックを提供するサービスです。
 * 【Java機能アピール】Stream API、Optional、ラムダ式の積極活用
 */
@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    // 【実務標準】コンストラクタインジェクションによるDI（依存性の注入）
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 全てのタスクを取得し、DTOのリストに変換して返します。
     * 【Java機能アピール】Stream APIとメソッド参照による宣言的プログラミング
     */
    public List<TaskResponseDto> getAllTasks() {
        return taskRepository.findAll().stream()
                // 各TaskエンティティをTaskResponseDto（Record）にマッピング
                .map(task -> new TaskResponseDto(
                        task.getId(),
                        task.getTitle(),
                        task.getDescription(),
                        task.getStatus(),
                        task.getCreatedAt()
                ))
                .toList(); // Java 16以降のスマートなリスト化
    }

    /**
     * 新しいタスクを登録します。
     */
    @Transactional
    public TaskResponseDto createTask(TaskRequestDto requestDto) {
        // Enumの独自メソッドとOptionalを組み合わせた安全な変換
        TaskStatus status = TaskStatus.of(requestDto.status())
                .orElse(TaskStatus.TODO); // 不正な値ならデフォルトでTODOに

        Task task = new Task(requestDto.title(), requestDto.description(), status);
        Task savedTask = taskRepository.save(task);

        return convertToResponseDto(savedTask);
    }

    /**
     * 既存のタスクを更新します。
     * 【Java機能アピール】Optionalによるnull安全な例外ハンドリング
     */
    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto requestDto) {
        // 【アンチパターン対策】nullを直接返したりチェックしたりせず、Optionalで例外をスロー
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたタスクが見つかりません。ID: " + id));

        TaskStatus status = TaskStatus.of(requestDto.status())
                .orElseThrow(() -> new IllegalArgumentException("不正なステータスコードです: " + requestDto.status()));

        // エンティティの状態を更新
        task.setTitle(requestDto.title());
        task.setDescription(requestDto.description());
        task.setStatus(status);

        // ダーティチェッキング（自動更新確認）により、saveを呼ばずともコミット時にDB更新されます
        return convertToResponseDto(task);
    }

    /**
     * タスクを削除します。
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("削除対象のタスクが見つかりません。ID: " + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * EntityからResponseDtoへの共通マッピング処理
     */
    private TaskResponseDto convertToResponseDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
