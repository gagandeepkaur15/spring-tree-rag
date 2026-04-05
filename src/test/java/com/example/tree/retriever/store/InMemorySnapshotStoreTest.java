package com.example.tree.retriever.store;

import com.example.tree.retriever.core.StoreSnapshot;
import com.example.tree.retriever.core.StoreStats;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemorySnapshotStoreTest {

    @Test
    void putPublishesSnapshotOnlyWhenValueChanges() {
        InMemorySnapshotStore<String, String> store = new InMemorySnapshotStore<>();

        boolean firstPut = store.put("a", "1");
        StoreSnapshot<String, String> firstSnapshot = store.snapshot();
        boolean secondPutWithSameValue = store.put("a", "1");
        StoreSnapshot<String, String> secondSnapshot = store.snapshot();
        boolean thirdPutWithNewValue = store.put("a", "2");
        StoreSnapshot<String, String> thirdSnapshot = store.snapshot();

        assertThat(firstPut).isTrue();
        assertThat(secondPutWithSameValue).isFalse();
        assertThat(thirdPutWithNewValue).isTrue();

        assertThat(firstSnapshot.version()).isEqualTo(1L);
        assertThat(secondSnapshot.version()).isEqualTo(1L);
        assertThat(thirdSnapshot.version()).isEqualTo(2L);
        assertThat(thirdSnapshot.entries()).containsEntry("a", "2");
    }

    @Test
    void removePublishesSnapshotOnlyWhenEntryExists() {
        InMemorySnapshotStore<String, String> store = new InMemorySnapshotStore<>();
        store.put("a", "1");

        StoreSnapshot<String, String> beforeRemove = store.snapshot();
        boolean removed = store.remove("a");
        StoreSnapshot<String, String> afterRemove = store.snapshot();
        boolean removedMissing = store.remove("a");
        StoreSnapshot<String, String> afterMissingRemove = store.snapshot();

        assertThat(removed).isTrue();
        assertThat(removedMissing).isFalse();
        assertThat(afterRemove.version()).isEqualTo(beforeRemove.version() + 1);
        assertThat(afterRemove.entries()).doesNotContainKey("a");
        assertThat(afterMissingRemove.version()).isEqualTo(afterRemove.version());
    }

    @Test
    void snapshotsAreImmutable() {
        InMemorySnapshotStore<String, String> store = new InMemorySnapshotStore<>();
        store.put("a", "1");

        StoreSnapshot<String, String> snapshot = store.snapshot();

        assertThatThrownBy(() -> snapshot.entries().put("b", "2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void statsExposeMetricsFriendlyCounters() {
        InMemorySnapshotStore<String, String> store = new InMemorySnapshotStore<>();

        store.put("a", "1");
        store.put("a", "1");
        store.put("a", "2");
        store.get("a");
        store.get("missing");
        store.remove("a");
        store.remove("a");

        StoreStats stats = store.stats();

        assertThat(stats.writeAttempts()).isEqualTo(3L);
        assertThat(stats.successfulWrites()).isEqualTo(2L);
        assertThat(stats.reads()).isEqualTo(2L);
        assertThat(stats.readHits()).isEqualTo(1L);
        assertThat(stats.readMisses()).isEqualTo(1L);
        assertThat(stats.removeAttempts()).isEqualTo(2L);
        assertThat(stats.successfulRemoves()).isEqualTo(1L);
        assertThat(stats.snapshotPublications()).isEqualTo(3L);
        assertThat(stats.currentSize()).isEqualTo(0L);
    }
}
