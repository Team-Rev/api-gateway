package rev.team.API_GATEWAY.contorller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import rev.team.API_GATEWAY.models.AuthenticationRequest;
import rev.team.API_GATEWAY.models.AuthenticationResponse;
import rev.team.API_GATEWAY.user.service.RevUserService;
import rev.team.API_GATEWAY.util.JwtUtil;

import java.util.Objects;

@RestController
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final RevUserService userDetailsService;
    private final JwtUtil jwtTokenUtil;

    @Autowired
    public AuthenticationController(RevUserService userDetailsService
            , JwtUtil jwtTokenUtil
            ,AuthenticationManager authenticationManager){
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest)throws Exception{
        RestTemplate api = new RestTemplate();
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }catch (BadCredentialsException e){
            throw new Exception("Incorrect username or password", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        String nickname = api.getForEntity("http://localhost:8775/nickname?username="+authenticationRequest.getUsername(),String.class).getBody();
        return ResponseEntity.ok(new AuthenticationResponse(jwt, nickname));
    }


    //Refresh Token
    
    @GetMapping("/point")
    public Long getPoint(@RequestParam String id) {
        return userDetailsService.findUser(id).orElse(null).getPoint();
    }
}
