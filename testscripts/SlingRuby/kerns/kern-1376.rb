#!/usr/bin/env ruby

require 'rubygems'
require 'bundler'
Bundler.setup(:default)
require 'nakamura/test'
require 'nakamura/file'
include SlingUsers
include SlingFile

class TC_Kern1376Test < Test::Unit::TestCase
  include SlingTest

  def test_get_pooled_content_activities
    @fm = FileManager.new(@s)
    m = uniqueness()
    manager = create_user("user-manager-#{m}")
    @s.switch_user(User.admin_user())
    res = @fm.upload_pooled_file("random-#{m}.txt", "Plain content", "text/plain")
    assert_equal("201", res.code, "Expected to be able to create pooled content")
    uploadresult = JSON.parse(res.body)
    contentid = uploadresult["random-#{m}.txt"]['poolId']
    assert_not_nil(contentid, "Should have uploaded ID")
    contentpath = @s.url_for("/p/#{contentid}")
    @log.info("Content path = #{contentpath}")

    # modify metadata and make sure user can write it
    res = @s.execute_post("#{contentpath}.html", { "testing" => "testvalue" })
    assert_equal("200", res.code, " #{manager.name} should have been granted write to #{contentpath} ")
    @log.info(" #{manager.name} can write to the resource ")
    res = @s.execute_get("#{contentpath}.tidy.json")
    assert_equal("200", res.code, "Unable to get the metadata for the resource ")
    m = JSON.parse(res.body)
    assert_equal(m["testing"], "testvalue", "Looks like the property was not written Got #{res.body}")

    wait_for_indexer()
    sleep(5)

    res = @s.execute_get(@s.url_for("/var/search/activity/pooledcontent.tidy.json"), {
      "p" => "/p/#{contentid}"
    })
    assert_equal("200", res.code, "Should have found activity feed")
    @log.info("Activity feed is #{res.body}")
    activityfeed = JSON.parse(res.body)
    # the system should have create created one activity
    # sakai:activityMessage=CREATED_FILE
    assert_equal(1, activityfeed["total"])
    assert_equal("CREATED_FILE", activityfeed["results"][0]["sakai:activityMessage"])

  end

end
