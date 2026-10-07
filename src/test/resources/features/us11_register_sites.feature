Feature: US11 - Register sites
  As a coworking administrator
  I want to register my site in the platform
  So that its rooms and devices can be monitored

  Scenario: The administrator registers a new site
    Given no site with code "coworking-lima-centro" is registered
    And I am signed in as "ADMIN"
    When I register the site "coworking-lima-centro" named "Coworking Lima Centro" without a timezone
    Then the response status is 201
    And the registered site has code "coworking-lima-centro" and timezone "America/Lima"

  Scenario: A site code can only be used once
    Given the site "coworking-lima-centro" is already registered
    And I am signed in as "ADMIN"
    When I register the site "coworking-lima-centro" named "Another site" without a timezone
    Then the response status is 409
    And the error code is "MONITORING_SITE_CODE_ALREADY_USED"

  Scenario: A member cannot register sites
    Given no site with code "coworking-miraflores" is registered
    And I am signed in as "MEMBER"
    When I register the site "coworking-miraflores" named "Coworking Miraflores" without a timezone
    Then the response status is 403
    And the error code is "API_FORBIDDEN"
    And no site is stored
