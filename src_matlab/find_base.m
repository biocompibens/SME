function [base]=find_base(IB,k)
% k=3;
loop=1;
sz1=size(IB,1)-k+1;
sz2=size(IB,2)-k+1;
for inx=1:k
    for iny=1:k
        
        base(:,:,loop)=IB(inx:inx+sz1-1,iny:iny+sz2-1);
        loop=loop+1;
    end
end

base(:,:,ceil((k^2)/2))=[];



% if k==3
% base=cat(3,IB(1:sz1,1:sz2),IB(2:sz1+1,1:sz2),IB(1:sz1,2:sz2+1),IB(3:sz1+2,1:sz2),IB(1:sz1,3:sz2+2),IB(2:sz1+1,3:sz2+2),IB(3:sz1+2,2:sz2+1),IB(3:sz1+2,3:sz2+2));
% elseif k==5
%  base=cat(3,IB(1:sz1,1:sz2),IB(2:sz1+1,1:sz2),IB(1:sz1,2:sz2+1),IB(3:sz1+2,1:sz2),IB(1:sz1,3:sz2+2),IB(2:sz1+1,3:sz2+2),IB(3:sz1+2,2:sz2+1),IB(3:sz1+2,3:sz2+2),
%  IB(3:sz1+2,3:sz2+2)...);   
% end