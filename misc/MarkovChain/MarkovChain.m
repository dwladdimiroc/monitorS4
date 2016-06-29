function [Prediction] = MarkovChain(Lambda,Mu,N,Iteration)

    % λ = lambda
    % num = [1 N];
    % Tl = normrnd(1000,500,num);
    % Tl = poissrnd(2000,num);
    Tl = Lambda;

    for i = 1:N
        if( Tl(i) < 0 )
            Tl(i) = 0;
        end
    end

    % μ = mu
    % num = [1 N];
    % Ts = normrnd(1000,250,num);
    Ts = Mu;

    for i = 1:N
        if( Ts(i) < 0 )
            Ts(i) = 0;
        end
    end

    % Cola del sistema
    C = [];

    C(1) = Tl(1) - Ts(1);
    if(C(1) < 0)
        C(1) = 0;
    end

    for i=2:N
        C(i) = C(i-1) + (Tl(i) - Ts(i));
        if(C(i) < 0)
            C(i) = 0;
        end
    end


    % Tasa de procesamiento
    Tp = Tl ./ Ts;
    for i=1:N
        if(Tp(i) == Inf)
            Tp(i) = 0;
        end
    end

    % Cadena de Markov
    P = zeros(3);
    Cont = [0 0 0];

    for i=1:(N-1)
        %Analisis de no-replicacion
        if (Tp(i) < 0.5) && (Tp(i+1) < 0.5)
            P(1,1) = P(1,1) + 1;
            Cont(1) = Cont(1) + 1;
        elseif (Tp(i) < 0.5) && (Tp(i+1) >= 0.5) && (Tp(i+1) <= 1.5)
            P(1,2) = P(1,2) + 1;
            Cont(1) = Cont(1) + 1;
        elseif (Tp(i) < 0.5) && (Tp(i+1) > 1.5)
            P(1,3) = P(1,3) + 1;
            Cont(1) = Cont(1) + 1;
        %Analisis de estable    
        elseif (Tp(i) >= 0.5) && (Tp(i) <= 1.5) && (Tp(i+1) < 0.5)
            P(2,1) = P(2,1) + 1;
            Cont(2) = Cont(2) + 1;
        elseif (Tp(i) >= 0.5) && (Tp(i) <= 1.5) && (Tp(i+1) >= 0.5) && (Tp(i+1) <= 1.5)
            P(2,2) = P(2,2) + 1;
            Cont(2) = Cont(2) + 1;
        elseif (Tp(i) >= 0.5) && (Tp(i) <= 1.5) && (Tp(i+1) > 1.5)
            P(2,3) = P(2,3) + 1;
            Cont(2) = Cont(2) + 1;
        %Analisis de replicacion    
        elseif (Tp(i) > 1.5) && (Tp(i) < 0.5)
            P(3,1) = P(3,1) + 1;
            Cont(3) = Cont(3) + 1;
        elseif (Tp(i) > 1.5) && (Tp(i+1) >= 0.5) && (Tp(i+1) <= 1.5)
            P(3,2) = P(3,2) + 1;
            Cont(3) = Cont(3) + 1;
        else
            P(3,3) = P(3,3) + 1;
            Cont(3) = Cont(3) + 1;
        end

    end

    % Normalizar las probabilidades
    if(Cont(1) ~= 0)
        P(1,:) = P(1,:)./Cont(1);
    end

    if(Cont(2) ~= 0)
        P(2,:) = P(2,:)./Cont(2);
    end

    if(Cont(3) ~= 0)
        P(3,:) = P(3,:)./Cont(3);
    end

    C = [0 0 0];

    for k=1:3
        if(~isequal(P(k,:),C))
            i = k;
            break;
        end
    end

    for n = 1:Iteration
        u = rand;
        probAcum = 0;
        for j=1:3
            probAcum = probAcum + P(i,j);
            if u <= probAcum
                C(j)=C(j)+1;
                i = j;
                break
            end
        end
    end

    Prediction = C/Iteration;

end