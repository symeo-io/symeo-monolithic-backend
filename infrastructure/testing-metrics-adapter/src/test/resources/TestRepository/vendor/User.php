<?php

namespace entities\busit;

/**
 * Represents a User
 */
class User
{
    /**
     * The user id
     * @var int
     */
    public $id;

    /**
     * The user name
     * @var string|null
     */
    public $name;

    /**
     * The language used by the user
     * @var string|null
     */
    public $language;

    /**
     * @param int $id The user id
     * @param string $name The user name
     * @param string $language The language used by the user
     */
    public function __construct($id, $name = null, $language = null)
    {
        $this->id = $id;
        $this->name = $name;
        $this->language = $language;
    }

    /**
     * @param array $json JSON array representing the User
     *
     * @return User
     */
    public static function fromJSON($json)
    {
        return new User(
            $json['id'],
            $json['name'],
            $json['language']
        );
    }

    /**
     * @return array JSON array representing this user
     */
    public function toJSON()
    {
        return array(
            'id' => $this->id,
            'name' => $this->name,
            'language' => $this->language
        );
    }
}

