<?php

namespace services\busit;

use React\Promise\Promise;
use cache\UserQosCache;
use constants\Constants;
use util\rest\RestApi;

/**
 * Get informations about a User
 */
class UserService
{
    /**
     * @var RestApi
     */
    private $client;

    /**
     * @var UserQosCache|null
     */
    private $cache;

    /**
     * @param RestApi $client Api client
     * @param UserQosCache|null $cache Cache instance, no cache if null
     */
    public function __construct($client, $cache = null)
    {
        $this->client = $client;
        $this->cache = $cache;
    }

    /**
     * @param int $user The user id
     *
     * @return Promise ReactPHP promise resolving the QOS name or id for this user
     */
    public function getUserQOS($user)
    {
        if ($this->cache !== null && $qos = $this->cache->getUserQOSCache($user)) {
            return \React\Promise\resolve($qos);
        } else {
            return $this->client->request(Constants::$api_roots['userQOS'], array('user' => $user))
                ->then(function ($result) use ($user) {
                    $qos = isset($result['qos_id']) ? $result['qos_id'] : $result['qos_name'];
                    $this->cache === null ?: $this->cache->setUserQOSCache($user, $qos);
                    return \React\Promise\resolve($qos);
                });
        }
    }
}

