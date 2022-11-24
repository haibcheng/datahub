package com.linkedin.metadata.resources.entity.datasource;

import com.datahub.authentication.Authentication;
import com.linkedin.common.AuditStamp;
import com.linkedin.common.urn.Urn;
import com.linkedin.entity.client.EntityClient;
import com.linkedin.events.metadata.ChangeType;
import com.linkedin.identity.CorpUserInfo;
import com.linkedin.identity.CorpUserStatus;
import com.linkedin.metadata.entity.EntityService;
import com.linkedin.metadata.utils.GenericRecordUtils;
import com.linkedin.mxe.MetadataChangeProposal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.Objects;

import static com.linkedin.metadata.Constants.*;


/**
 * Service responsible for creating, updating and authenticating native DataHub users.
 */
@Slf4j
@RequiredArgsConstructor
public class NativeUserService {
  private final EntityService _entityService;
  private final EntityClient _entityClient;

  public void createNativeUser(@Nonnull String userUrnString, @Nonnull String fullName, @Nonnull String email,
      @Nonnull String title, @Nonnull String password, @Nonnull Authentication authentication) throws Exception {
    Objects.requireNonNull(userUrnString, "userUrnSting must not be null!");
    Objects.requireNonNull(fullName, "fullName must not be null!");
    Objects.requireNonNull(email, "email must not be null!");
    Objects.requireNonNull(title, "title must not be null!");
    Objects.requireNonNull(password, "password must not be null!");
    Objects.requireNonNull(authentication, "authentication must not be null!");

    Urn userUrn = Urn.createFromString(userUrnString);
    if (_entityService.exists(userUrn)) {
      throw new RuntimeException("This user already exists! Cannot create a new user.");
    }
    updateCorpUserInfo(userUrn, fullName, email, title, authentication);
    updateCorpUserStatus(userUrn, authentication);
  }

  void updateCorpUserInfo(@Nonnull Urn userUrn, @Nonnull String fullName, @Nonnull String email, @Nonnull String title,
      Authentication authentication) throws Exception {
    // Construct corpUserInfo
    final CorpUserInfo corpUserInfo = new CorpUserInfo();
    corpUserInfo.setFullName(fullName);
    corpUserInfo.setDisplayName(fullName);
    corpUserInfo.setEmail(email);
    corpUserInfo.setTitle(title);
    corpUserInfo.setActive(true);

    // Ingest corpUserInfo MCP
    final MetadataChangeProposal corpUserInfoProposal = new MetadataChangeProposal();
    corpUserInfoProposal.setEntityType(CORP_USER_ENTITY_NAME);
    corpUserInfoProposal.setEntityUrn(userUrn);
    corpUserInfoProposal.setAspectName(CORP_USER_INFO_ASPECT_NAME);
    corpUserInfoProposal.setAspect(GenericRecordUtils.serializeAspect(corpUserInfo));
    corpUserInfoProposal.setChangeType(ChangeType.UPSERT);
    _entityClient.ingestProposal(corpUserInfoProposal, authentication);
  }

  void updateCorpUserStatus(@Nonnull Urn userUrn, Authentication authentication) throws Exception {
    // Construct corpUserStatus
    CorpUserStatus corpUserStatus = new CorpUserStatus();
    corpUserStatus.setStatus(CORP_USER_STATUS_ACTIVE);
    corpUserStatus.setLastModified(
        new AuditStamp().setActor(Urn.createFromString(SYSTEM_ACTOR)).setTime(System.currentTimeMillis()));

    // Ingest corpUserStatus MCP
    final MetadataChangeProposal corpUserStatusProposal = new MetadataChangeProposal();
    corpUserStatusProposal.setEntityType(CORP_USER_ENTITY_NAME);
    corpUserStatusProposal.setEntityUrn(userUrn);
    corpUserStatusProposal.setAspectName(CORP_USER_STATUS_ASPECT_NAME);
    corpUserStatusProposal.setAspect(GenericRecordUtils.serializeAspect(corpUserStatus));
    corpUserStatusProposal.setChangeType(ChangeType.UPSERT);
    _entityClient.ingestProposal(corpUserStatusProposal, authentication);
  }

}