/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogEntryComment.ApprovalStatus;

import java.util.Collections;
import java.util.List;

@Repository
@Transactional("transactionManager")
public interface WeblogEntryCommentRepository extends JpaRepository<WeblogEntryComment, String> {

    // method should be used with care as it returns all comments regardless of approval status
    List<WeblogEntryComment> findByWeblogEntry(WeblogEntry e);

    default List<WeblogEntryComment> findByWeblogEntryAndStatusApproved(WeblogEntry entry) {
        return findByWeblogEntryAndStatusInOrderByPostTimeAsc(entry,
                Collections.unmodifiableList(List.of(ApprovalStatus.APPROVED)));
    }

    List<WeblogEntryComment> findByWeblogEntryAndStatusInOrderByPostTimeAsc(WeblogEntry entry,
                                                                            List<ApprovalStatus> statuses);

    int countByWeblogEntry(WeblogEntry e);

    default int countByWeblogEntryAndStatusApproved(WeblogEntry entry) {
        return countByWeblogEntryAndStatusIn(entry, Collections.unmodifiableList(List.of(ApprovalStatus.APPROVED)));
    }

    int countByWeblogEntryAndStatusIn(WeblogEntry entry, List<ApprovalStatus> statuses);

    void deleteByWeblogEntry(WeblogEntry e);

    default WeblogEntryComment findByIdOrNull(String id) {
        return findById(id).orElse(null);
    }

}