package com.issuetracker.oauth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.issuetracker.repository.UserRepository;
import com.issuetracker.util.Oauth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    private Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final Oauth oauthUtil;
    // TODO: 추후 서비스 로직 분리 시 가져갈 것
    private final UserRepository userRepository;

    public LoginController(Oauth oauthUtil, UserRepository userRepository) {
        this.oauthUtil = oauthUtil;
        this.userRepository = userRepository;
    }

    @GetMapping("/auth")
    public JwtDto login(@RequestParam String client, @RequestParam String code) {
        RestTemplate githubRequest = new RestTemplate();
        String accessTokenUri = oauthUtil.getUriForAccesToken(code);
        RequestEntity<GithubAccessTokenRequestDto> requestDto = null;

        // TODO: iOS 파트 추가//
        if (client.equals("ios")) {
            requestDto = RequestEntity
                    .post(accessTokenUri)
                    .header("Accept", "application/json")
                    .body(new GithubAccessTokenRequestDto(
                            oauthUtil.getClientIdIos(), oauthUtil.getClientSecretIos(), code, oauthUtil.getRedirectUriIos()
                    ));
        }

        if (client.equals("web")) {
            requestDto = RequestEntity
                    .post(accessTokenUri)
                    .header("Accept", "application/json")
                    .body(new GithubAccessTokenRequestDto(
                            oauthUtil.getClientId(), oauthUtil.getClientSecret(), code, oauthUtil.getRedirectUri()
                    ));
        }

        ResponseEntity<GithubAccessTokenResponseDto> responseDto = githubRequest.exchange(requestDto, GithubAccessTokenResponseDto.class);

        RequestEntity<Void> request = RequestEntity
                .get(oauthUtil.getUserinfoUri())
                .header("Accept", "application/json")
                .header("Authorization", "token " + responseDto.getBody().getAccessToken())
                .build();

        ResponseEntity<User> user = githubRequest.exchange(request, User.class);

        User loginUser = user.getBody();
        if (!userRepository.hasSameUserId(loginUser.getId())) {
            userRepository.save(loginUser);
        }

        Algorithm algorithm = Algorithm.HMAC256(oauthUtil.getAlgorithmSecret());

        String jwt = JWT.create()
                .withClaim("id", user.getBody().getId())
                .withClaim("name", user.getBody().getLogin())
                .withClaim("avatar_url", user.getBody().getAvatar_url())
                .withIssuer(oauthUtil.getIssuer())
                .sign(algorithm);

        return new JwtDto(jwt);
    }
}

