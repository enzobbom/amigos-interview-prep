# URL Shortening: Bases, Encoding, Hashing and Collision Handling

## 1. What is a base?

- A base defines how numbers are represented using a set of symbols
- Decimal = base 10, binary = base 2, hexadecimal = base 16, Base62 = 62 symbols

| Base                 | Symbols used                    | Example: decimal 125 |
| -------------------- | ------------------------------- | -------------------- |
| Binary (Base2)       | `0–1`                           | `1111101`            |
| Decimal (Base10)     | `0–9`                           | `125`                |
| Hexadecimal (Base16) | `0–9`, `A–F`                    | `7D`                 |
| Base62               | `0–9`, `a–z`, `A–Z`             | `21`                 |
| Base64               | `A–Z`, `a–z`, `0–9`, `+`, `/`\* | Depends on encoding  |

\*Standard Base64 uses these 64 symbols; URL-safe Base64 uses `-` and `_` instead of `+` and `/`.

### How does Base62 represent 125 as `21`?

- Decimal `125`: $1 \times 10^2 + 2 \times 10^1 + 5 \times 10^0$

- Base62 `21`: $2 \times 62^1 + 1 \times 62^0 = 125$

- Same number, different representation depending on the base

Base62 alphabet:

```
private static final String BASE62 =
        "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
```

- Index `0` = zero, index `1` = one, ... up to index `61`

### Converting a decimal integer to Base62

- Repeatedly divide by the target base (62); each division gives a **quotient** and a **remainder**
- The **remainder** is the next "digit" of the result:
  - It is a number from `0` to `61`
  - That number is an **index** into the Base62 alphabet
  - The character at that index is the output digit: `BASE62.charAt(remainder)`
- The **quotient** is divided again in the next round, until it reaches `0`
- The last digit comes out first, so the characters are read **bottom to top**

| Index (remainder) | Character |
| ----------------- | --------- |
| `0`–`9`           | `0`–`9`   |
| `10`–`35`         | `a`–`z`   |
| `36`–`61`         | `A`–`Z`   |

Example 1: decimal `125`

| Operation | Quotient | Remainder (index) | Character |
| --------- | -------- | ----------------- | --------- |
| 125 ÷ 62  | 2        | 1                 | `1`       |
| 2 ÷ 62    | 0        | 2                 | `2`       |

- Read bottom to top: `21`
- Looks like the decimal digits only because indexes `0`–`9` map to the digits themselves

Example 2: decimal `5000000` (remainders above 9 give letters)

| Operation       | Quotient | Remainder (index) | Character |
| --------------- | -------- | ----------------- | --------- |
| 5000000 ÷ 62    | 80645    | 10                | `a`       |
| 80645 ÷ 62      | 1300     | 45                | `J`       |
| 1300 ÷ 62       | 20       | 60                | `Y`       |
| 20 ÷ 62         | 0        | 20                | `k`       |

- Read bottom to top: `kYJa`
- Check: $20 \times 62^3 + 60 \times 62^2 + 45 \times 62^1 + 10 \times 62^0 = 5{,}000{,}000$
- Same process for huge numbers (like the SHA-256 integer in section 3): just more rounds, one character per round

In Java, `BigInteger.divideAndRemainder()` returns both values at once:

```
BigInteger[] result =
        number.divideAndRemainder(BigInteger.valueOf(62));

BigInteger quotient = result[0];   // divided again in the next round
BigInteger remainder = result[1];  // index of the next character
```

> **Important:** Base62 does not compress arbitrary text. It represents an integer with a different set of symbols, which can be shorter than decimal or hex.

### How much information does one character hold?

- Computers store data as **bits**: a bit is a single `0`/`1` choice
- `n` bits can represent $2^n$ different values:

| Bits | Different values |
| ---- | ---------------- |
| 1    | 2                |
| 4    | 16               |
| 6    | 64               |
| 8    | 256              |

- So the number of symbols in an alphabet tells how many bits one character carries:

| Alphabet                          | Symbols per position | ≈ Bits per character                |
| --------------------------------- | -------------------- | ----------------------------------- |
| Binary                            | 2                    | 1                                   |
| Hexadecimal                       | 16                   | 4                                   |
| Base62                            | 62                   | ~6 (62 is just under 64 = $2^6$)    |
| A byte (one typical URL character) | 256                 | 8                                   |

- Fewer bits per character → more characters needed for the same information
  - A SHA-256 digest has 256 bits: 64 hex characters (256 ÷ 4) or ~43 Base62 characters (256 ÷ ~6)
  - A URL character takes 8 bits, but a Base62 character carries only ~6 → the same information needs ~1.3× more characters

## 2. Encoding, hashing, and encryption are different

| Concept    | Purpose                                                      | Reversible?                                  | Example                            |
| ---------- | ------------------------------------------------------------ | -------------------------------------------- | ---------------------------------- |
| Encoding   | Represent data in a different format                         | Yes, when the encoding is lossless           | Integer `125` → Base62 `21`        |
| Hashing    | Produce a fixed-size digest from input data                  | Not practically reversible for a secure hash | URL → SHA-256 digest               |
| Encryption | Protect data so it can be recovered with the appropriate key | Yes, with the correct key and method         | Plaintext → ciphertext → plaintext |

> **Digest:** the fixed-size output of a hash function, like a short "fingerprint" of the input. The same input always gives the same digest; a tiny change in the input gives a completely different one.

### Hashing with SHA-256

- **Cryptographic hash function:** a hash function built to be one-way and to make collisions practically impossible to find
  - Unlike encryption, there is no key, and nothing is meant to be "decrypted" back
- Arbitrary-length input → 256-bit digest (64 hex characters)
- Result is a 32-byte array (256 bits ÷ 8)

```
MessageDigest digest = MessageDigest.getInstance("SHA-256");

byte[] hash = digest.digest(longUrl.getBytes(StandardCharsets.UTF_8));
```

- Deterministic: same input → same digest
- Computationally infeasible to:
  - recover the input from the digest
  - find two inputs with the same digest
- A hash is **not** encryption: no decryption operation recovers the original URL
- Why can't it be reversed if it is deterministic?
  - The output is always 256 bits, whatever the input size → information is thrown away
  - Many different inputs share the same digest (finding two is infeasible, but they exist), so the digest can't say which one was used
  - Analogy: `17 mod 5 = 2`. The same input always gives `2`, but from `2` you can't tell if it came from 7, 12 or 17
  - SHA-256 also scrambles the bits so thoroughly that the digest shows no trace of the input's structure

## 3. Hashing a URL and converting the result to Base62

- Goal: transform a long URL into a compact short code

### Why hash first?

- Base62 can only encode an **integer**, not text → we first need a number
- Why not turn the URL's bytes straight into a number?
  - Encoding changes the representation, not the amount of information
  - Each URL character takes 8 bits, while each Base62 character carries only ~6 (see "How much information does one character hold?" in section 1)
  - So the same information needs more Base62 characters (~1.3×) → the code would be *longer* than the URL, not shorter
- A hash gives us a number that is:
  - **Fixed-size:** a 40-character URL and a 2,000-character URL both become 256 bits
  - **Well spread:** similar URLs (same domain, same path prefix) become completely unrelated numbers, so cutting the result down to 8 characters (section 4) collides as rarely as the space allows
- Hashing is **not the only way** to get that number. Other designs replace it:

| Where the number comes from | How it replaces the hash                                | Trade-off                                                     |
| --------------------------- | ------------------------------------------------------- | ------------------------------------------------------------- |
| Hash of the URL (this exercise) | Number derived from the URL itself                  | Fixed-size and well spread, but cutting to 8 characters can collide |
| Database ID / counter       | Database assigns `125` → Base62 `21`; no hash needed    | No collisions, but codes are sequential and reveal creation order |
| Random number               | Generate a random value → Base62 → check it's not taken | Unpredictable, but collisions grow as the space fills         |

- In our exercise, a timestamp + UUID are mixed into the hash input (section 5)
  - The UUID is different on every call, so the same URL gets a **different code every time**
  - The URL is just one ingredient: it no longer determines the code on its own
  - So the hash is effectively acting as a **random number generator**
  - That makes it very close to the "random number" row above
- In every design we still store **code → URL** in a map or database
  - The code is never decoded back into the URL
  - A hash can't be reversed, and 8 characters can't hold an arbitrary URL

### The pipeline

- Example URL → SHA-256 → positive integer → Base62:

```
  LONG URL
  https://example.com/articles/how-to-learn-java
        │
        │  SHA-256
        ▼
  DIGEST  ·  32 bytes (256 bits)  ·  64 hex characters
  cddf99d0db76bdf50b86b00eb80dc7dad54f00680a70cff64d27daab5d7e7113
        │
        │  read the bytes as a positive number  (new BigInteger(1, hash))
        ▼
  POSITIVE INTEGER  ·  up to 78 digits
  93119202452987394554938992310303289632229919852196319544951808427190927651091
        │
        │  repeatedly ÷ 62, collect remainders
        ▼
  BASE62 STRING  ·  up to ~43 characters
  MOLYOvRqhCpBEnCRTeWpQUwKcqFje8Sdxr8UmZ9UXyX
```

- Only the first step is hashing; the next two just rewrite the same number in another form
- The `1` in `new BigInteger(1, hash)` means positive sign
- Without it, Java may read the bytes as signed two's complement → possibly negative

### The digest has letters, but Base62 needs a number?

- The digest is **not text**: it is 32 raw bytes, i.e. one big 256-bit **number**
- The letters (`a`–`f`) in `cddf99…` only appear because we *display* the bytes in **hexadecimal (Base16)**
  - Hex digits are `0`–`9` and then `a`–`f`, which stand for the values 10–15
- Hex is just another base: same number, different symbols (like Base62 in section 1)
  - Example from the table in section 1: hex `7D` → $7 \times 16 + 13 = 125$ → Base62 `21` (`D` = 13)
- So nothing converts "letters" into numbers
  - `new BigInteger(1, hash)` reads the bytes directly as a number
  - The hex string is only a human-readable view of those same bytes
- Our digest, written three ways (same number):

| Notation         | Symbols available | Length for our digest |
| ---------------- | ----------------- | --------------------- |
| Hex (Base16)     | 16                | 64 characters         |
| Decimal (Base10) | 10                | 77 digits             |
| Base62           | 62                | 43 characters         |

- More symbols per position → shorter text for the same number

### The problem: the code is too long

- A full SHA-256 digest in Base62 needs up to ~43 characters
- Still shorter than many URLs, but our goal is **8 characters**
- We need to reduce the integer to a smaller range

## 4. Reducing the digest to an eight-character code

- 62 possible characters per position
- Number of 8-character codes: $62^8 = 218,340,105,584,896$

- ≈ 218 trillion possible codes
- Constrain the hash integer with modulo:

```
private static final BigInteger BASE62_LIMIT =
        BigInteger.valueOf(62).pow(8);

BigInteger reduced = number.mod(BASE62_LIMIT);
```

- `reduced` is always between `0` and `BASE62_LIMIT - 1`
- Convert the reduced integer to Base62:

```
String shortCode = encodeBase62(reduced);
```

- Small numbers can give fewer than 8 characters (e.g. zero → `"0"`)
- Fix: pad on the left with zeroes

```
return "0".repeat(8 - encoded.length()) + encoded;
```

- Assumes `encoded.length()` ≤ 8, which holds for any value below $62^8$

### Visualizing the reduction

- Modulo keeps only the **remainder** after dividing
  - Decimal analogy: `7392 mod 1000 = 392` → it keeps the **last 3 digits**
  - Same idea in Base62: `mod 62^8` keeps the **last 8 Base62 characters**
- Continuing with the integer from section 3:

```
  POSITIVE INTEGER  ·  77 digits
  93119202452987394554938992310303289632229919852196319544951808427190927651091
        │
        │  mod 62^8   (62^8 = 218,340,105,584,896)
        ▼
  REDUCED INTEGER  ·  always between 0 and 218,340,105,584,895
  198516053795603
        │
        │  Base62 conversion
        ▼
  BASE62 STRING  ·  up to 8 characters
  UmZ9UXyX
        │
        │  pad on the left with "0" if shorter than 8   (not needed here)
        ▼
  SHORT CODE  ·  exactly 8 characters
  UmZ9UXyX
```

- The same thing seen on the full 43-character Base62 string from section 3:

```
  MOLYOvRqhCpBEnCRTeWpQUwKcqFje8Sdxr8UmZ9UXyX
  └────────── thrown away ──────────┘└─kept─┘
```

- The first 35 characters are discarded → different numbers can end in the same 8 characters (the collision risk below)

### What does modulo imply?

- Modulo is **lossy**: multiple integers can give the same remainder
- Two different URLs can produce the same 8-character code, even if SHA-256 has no collision for those inputs
- → We need collision handling

## 5. Varying the hash input with a timestamp and UUID

- Hashing only the URL → same URL always gives the same short code
- For this in-memory exercise, we want a different candidate code per request, as the same long URL should generate a new code every time 
- Add a timestamp and a UUID to the input:

```
String input = Instant.now()
        + ":"
        + UUID.randomUUID()
        + ":"
        + longUrl;
```

- Hash the combined input:

```
byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
```

- What each part does:
  - **Timestamp:** introduces a time-dependent value
  - **UUID:** random value that makes repeated inputs highly unlikely
  - **Long URL:** the URL being shortened
- Caveats:
  - The timestamp is not secret
  - The UUID does not guarantee a unique short code
  - SHA-256 is still deterministic: identical inputs → identical digests
- The input varies between calls, but collisions are **not** eliminated after reducing to 8 Base62 characters

### Why vary the input at all?

- SHA-256 is deterministic → hashing only the URL gives the same code for the same URL, every time
- That conflicts with the retry loop from section 6:
  - The loop asks for another candidate when a code is already in the map
  - With a deterministic hash, "another candidate" is the **same code** → it would loop forever
  - Even the same URL submitted twice would get stuck: its own code is already in the map
- Timestamp + UUID make every call a fresh input → a fresh candidate on every retry
- It does **not** reduce collisions (the truncation to 8 characters causes them); it only lets a retry find a different code

## 6. Detecting and retrying collisions

- The application stores short code → original URL:

```
private final Map<String, String> urlsMap = new HashMap<>();
```

- Key = short code, value = long URL
- Check whether the key exists before saving:

```
String shortCode;

do {
    shortCode = encodeLongUrl(longUrl);
} while (urlsMap.containsKey(shortCode));

urlsMap.put(shortCode, longUrl);

return shortCode;
```

Process:

1. Generate a candidate code
2. Check whether it is already in the map
3. If it exists → generate another candidate
4. If not → save the mapping and return the code

- Prevents overwriting an existing URL under the same code (sequential execution)

### What happens if a collision occurs?

- The map already contains:

```
Ab12Cd34 -> https://example.com/first
```

- A second URL generates `Ab12Cd34` too
- The application detects the existing key and retries
- Once a code is free, the second URL is saved under that new code

> **Concurrency limitation:** `HashMap` and the separate `containsKey()`/`put()` calls are not thread-safe. Two simultaneous requests could both see a code as unused and both insert it. Production needs atomic collision handling, e.g. a database unique constraint with retry logic.

## 7. Putting the algorithm together

Shortening:

1. Validate the long URL
2. Combine timestamp + UUID + URL
3. Hash with SHA-256
4. Convert digest to a positive integer
5. Reduce modulo $62^8$ and convert to Base62
6. Pad to eight characters
7. Retry if the code already exists
8. Store code → long URL and return the code

### Expanding a short code

- Simpler: the original URL is stored in the map
- We do **not** reverse the hash

```
public Optional<String> expand(String shortCode) {
    if (!isValidShortCode(shortCode)) {
        throw new InvalidParameterException("Invalid short code");
    }

    return Optional.ofNullable(urlsMap.get(shortCode));
}
```

- Short-code validator:

```
private boolean isValidShortCode(String shortCode) {
    return shortCode != null
            && shortCode.matches("[a-zA-Z0-9]{8}");
}
```

- Checks for exactly 8 alphanumeric characters
- Does **not** check that the code exists in the map
- `Optional.ofNullable()` returns:
  - `Optional.of(url)` when a mapping exists
  - `Optional.empty()` when it doesn't
- The caller decides what to do with an unknown code (e.g. HTTP 404)

### Validating the original URL

- Basic URI-based validator for HTTP and HTTPS:

```
private boolean isValidURL(String url) {
    if (url == null || url.isBlank()) {
        return false;
    }

    try {
        URI uri = URI.create(url);

        return ("http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost() != null
                && !uri.getHost().isBlank();

    } catch (IllegalArgumentException e) {
        return false;
    }
}
```

- Checks syntax and structure (scheme and host)
- Does **not** check that the server exists, DNS resolves, or the URL is reachable

## 8. What would change in a production URL shortener?

- The algorithm above is good for understanding hashing, bases and collision handling
- Production systems would make different choices:

| Approach                     | How it works                                        | Main trade-off                                                                |
| ---------------------------- | --------------------------------------------------- | ----------------------------------------------------------------------------- |
| Unique ID + Base62           | Generate a unique database ID and encode it         | Simple collision avoidance, but codes may reveal approximate creation order   |
| Random short code            | Generate random characters and check uniqueness     | Unpredictable codes, but collisions become more likely as the namespace fills |
| Hash-based code              | Hash the URL and derive a short code                | Deterministic unless the input varies; truncation can cause collisions        |
| Database-enforced uniqueness | Let a unique constraint reject duplicates and retry | Protects against concurrent inserts, but requires atomic persistence logic    |

- Common design:
  - Database assigns each new URL a unique numeric ID
  - Application encodes the ID in Base62 → short code
  - Unique ID → unique code (if ID generation and storage are correct)
- Example: decimal ID `125` → Base62 `21` (pad to a fixed length if needed)
- A production service also needs:
  - Persistent storage
  - Concurrent request handling
  - Expiration policies (if applicable)
  - Rate limiting
  - Abuse prevention
  - Monitoring
  - Reliable redirect behavior

## Presentation summary

- Base62 represents integers compactly using 62 symbols
- Encoding changes representation; it is not hashing or encryption
- SHA-256 produces a deterministic 256-bit digest
- Modulo $62^8$ fits the result into 8 Base62 characters, but allows collisions
- Timestamp + UUID varies the hash input, but doesn't guarantee unique output
- Collision detection + retry prevents overwriting a mapping (sequential execution)
- Expansion is a lookup; the hash is never reversed
- Production needs persistent storage and atomic uniqueness enforcement for concurrent requests