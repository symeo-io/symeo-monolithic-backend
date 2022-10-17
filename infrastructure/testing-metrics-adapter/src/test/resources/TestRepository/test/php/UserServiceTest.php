<?php

use framework\TestCase;
use PHPUnit\Framework\MockObject\MockObject;
use services\busit\UserService;
use cache\UserQosCache;
use util\rest\RestApi;

final class UserServiceTest extends TestCase
{
    /**
     * @var RestApi&MockObject
     */
    private $restApi;

    /**
     * @var UserService
     */
    private $userService;

    protected function setUp(): void
    {
        parent::setUp();
        $this->restApi = $this->createMock(RestApi::class);
    }

    public function testGettingUserQOSFromApi()
    {
        $this->userService = new UserService($this->restApi);
        $this->restApi->method('request')->willReturn(
            $this->getFulfilledPromise(array(
                'qos_id' => 1
            ))
        );

        $qos = $this->waitForPromiseToFulfill($this->userService->getUserQOS(1));

        $this->assertEquals(1, $qos);
    }

    public function testGettingBusappFromCache()
    {
        $cache = new UserQosCache(900000);
        $this->userService = new UserService($this->restApi, $cache);
        $cache->setUserQOSCache(1, 1);
        $qos = $this->waitForPromiseToFulfill($this->userService->getUserQOS(1));

        $this->assertEquals(1, $qos);
    }
}

