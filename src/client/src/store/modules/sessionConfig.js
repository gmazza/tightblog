import axios from "axios";




  map.put("authenticatedUser", user);
  map.put("actionWeblog", weblog);
  map.put("actionWeblogURL", urlService.getWeblogURL(weblog));
  map.put("userIsAdmin", user != null && GlobalRole.ADMIN.equals(user.getGlobalRole()));
  map.put("mfaEnabled", mfaEnabled);
  map.put("tightblogVersion", tightblogVersion);
  map.put("tightblogRevision", tightblogRevision);
  map.put("registrationPolicy", webloggerPropertiesDao.findOrNull().getRegistrationPolicy());
