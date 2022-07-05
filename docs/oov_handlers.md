# Out-of-Vocabulary handlers

Not every word can and should be registered in the dictionary.
Words which are not present in the dictionary are handled by OOV handlers.

Sudachi configuration must include at least one OOV handler, otherwise Dictionary initialization fails with an error.
Also, last OOV handler is special, it **must** be able to produce a node for a boundary on which there were no other words.
Usually, the simple provider is used in this matter.

# Built-in handlers and configuration

Sudachi supports following built-in OOV handlers.

If your configuration uses default Sudachi configuration as a fallback, it is possible reorder OOV plugins
and override only a subset of parameters in the configuration by specifying only class name in the configuration file.

```json5
{
  "oovPlugins": [
    {
      "class": "com.worksap.nlp.sudachi.SimpleOovProviderPlugin"
    },
    { 
      "class" : "com.worksap.nlp.sudachi.MeCabOovProviderPlugin"
    }
  ]
}
```

This configuration will reorder OOV providers, so that Simple will be used before MeCab-compatible one,
and both plugins will use their default configurations.

## Simple OOV Handler

This handler provides a node from the requested boundary until the characters have the same class.
For example, when requested for boundary specified by | in 株式会社ワーク|スアプリケーションズ, it will produce スアプリケーションズ.

Character class mapping is defined in the file [char.def](https://github.com/WorksApplications/Sudachi/blob/develop/src/main/resources/char.def)
and should use types defined in [this enum](https://github.com/WorksApplications/Sudachi/blob/develop/src/main/java/com/worksap/nlp/sudachi/dictionary/CategoryType.java).

Sample configuration (all fields are required):

```json5
{ 
  "class" : "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
  "oovPOS" : [ "補助記号", "一般", "*", "*", "*", "*" ], 
  "leftId" : 5968,
  "rightId" : 5968,
  "cost" : 3857
}
```

## MeCab-compatible OOV handler
Sudachi supports [MeCab-compatible](https://taku910.github.io/mecab/unk.html) unknown word handling as well.

Default configuration:
```json5
{
 "class"   : "com.worksap.nlp.sudachi.MeCabOovProviderPlugin",
  "charDef" : "char.def", // optional, default: "char.def"
  "unkDef"  : "unk.def" // optional, default: "unk.def"
}
```

Refer to MeCab documentation for the format of `char.def` and `unk.def` files.

## Regex OOV Handler

Provides OOV nodes corresponding to a given regular expression.

Sample configuration:
```json5
 {
  "class": "com.worksap.nlp.sudachi.RegexOovProvider",
  "regex": "[0-9a-z-]+",                              // required   
  "leftId": 500,                                      // required
  "rightId": 500,                                     // required
  "cost": 5000,                                       // required
  "oovPOS": [ "補助記号", "一般", "*", "*", "*", "*" ], // required, oov is alias
  "userPOS": "allow",                                 // optional, default: "forbid"   
  "maxLength": 32,                                    // optional, default: 32
  "boundaries": "relaxed"                             // optional, default: "strict"
 }
```

Options:

* `regex` - a regular expression to match nodes. Backreferences and lookahead may are not portable and will not work in Rust/SudachiPy. Also, see section on boundaries below.
* `oovPOS` - POS of the OOV tag. User-defined POS are supported (see section on user-defined POS tags).
* `leftId`, `rightId`, `cost` - parameters for lattice node creation.
* `maxLength` - maximum length of the regular expression match. Current java version - bytes of utf8 representation. Current Rust version - number of codepoints.
* `boundaries` - specify boundary matching mode, see following section

### Boundary matching

By default, regex OOV handler can match only sequences of characters which start on a character type boundary.
For example, 靴|を|2|足|ABC|マート|で|買|った.  
Usually it is what you want, but sometimes it can not be the case, especially with symbols.

Relaxed matching mode makes it possible for the regex to match starting not on the character type boundary.
Still it is impossible to start match in the middle of the alphabetic or numeral group even in the relaxed mode.

As a rule of thumb:
* Use strict mode as a general case
* Consider using relaxed matching mode only if regex starts with a symbol


### Examples

Matching e-mails:
```json5
{
    "class": "com.worksap.nlp.sudachi.RegexOovProvider",    
    "leftId": 5968,
    "rightId": 5968,
    "cost": 500,
    "regex": "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[0-9a-z\\-]+\\.)+[a-z]{2,6}",
    "pos": [ "補助記号", "一般", "e-mail", "*", "*", "*" ],
    "userPOS": "allow"
}
```

Matching twitter @handles (minimum handle length - 4 symbols):
```json5
{
    "class": "com.worksap.nlp.sudachi.RegexOovProvider",
    "leftId": 5968,
    "rightId": 5968,
    "cost": 500,
    "regex": "^@[0-9a-z_\\.\\-‐]{4,}",
    "pos": [ "補助記号", "一般", "twitter", "*", "*", "*" ],
    "userPOS": "allow",
    "boundaries": "relaxed"
}
```

Matching URLs:
```json
{
  "class": "com.worksap.nlp.sudachi.RegexOovProvider",
  "leftId": 5968,
  "rightId": 5968,
  "cost": 500,
  "regex": "^(?:https?://|www)[\\-_.!~*'a-zA-Z0-9;/?:@&=+$,%#¯−―]+",
  "pos": [ "補助記号", "一般", "URL", "*", "*", "*" ],
  "userPOS": "allow"
}
```


# User-defined POS tags

All POS handlers support specifying customized POS tags for output.
In addition to specifying the user-defined POS tag, it is also necessary to set `"userPOS": "allow"`.
By default, Sudachi throws an exception if a POS tag not existing in the system dictionary was used.
User dictionaries are not considered for resolving POS tags of OOV handlers.

The usual caveat for user-defined POS tags still holds.
If the same user-defined POS tag was used in an OOV handler and a user dictionary, internally it will be created
at least twice with different POS id numbers.
String representation will be the same.
Be careful when using POS ids, they are not unique in this situation.

# Default configuration

Default configuration is in [sudachi.json](https://github.com/WorksApplications/Sudachi/blob/develop/src/main/resources/sudachi.json).

# Creating own OOV handlers

See current implementations for the inspiration.