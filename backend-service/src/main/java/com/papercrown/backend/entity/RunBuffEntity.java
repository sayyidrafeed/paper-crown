package com.papercrown.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "run_buffs")
public class RunBuffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private RunEntity run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buff_id", nullable = false)
    private BuffEntity buff;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "consumed", nullable = false)
    private boolean consumed;

    public RunBuffEntity() {}

    public RunBuffEntity(RunEntity run, BuffEntity buff) {
        this.run = run;
        this.buff = buff;
        this.appliedAt = LocalDateTime.now();
        this.consumed = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public RunEntity getRun() { return run; }
    public void setRun(RunEntity run) { this.run = run; }
    public BuffEntity getBuff() { return buff; }
    public void setBuff(BuffEntity buff) { this.buff = buff; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }
}
